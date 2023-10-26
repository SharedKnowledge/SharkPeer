package net.sharksystem;

import net.sharksystem.asap.ASAPEncounterManagerImpl;
import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.utils.DateTimeHelper;
import net.sharksystem.hub.hubside.ASAPTCPHub;
import net.sharksystem.hub.peerside.ASAPHubManager;
import net.sharksystem.hub.peerside.ASAPHubManagerImpl;
import net.sharksystem.hub.peerside.HubConnectorDescription;
import net.sharksystem.hub.peerside.TCPHubConnectorDescriptionImpl;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class SharkComponentTests {
    public static final CharSequence ALICE = "Alice";
    public static final CharSequence BOB = "Bob";

    public static final CharSequence CLARA = "Clara";
    static final CharSequence ROOTFOLDER = "sharkComponent";
    public static final CharSequence ALICE_ROOTFOLDER = ROOTFOLDER + "/" + ALICE;
    public static final CharSequence BOB_ROOTFOLDER = ROOTFOLDER + "/" + BOB;
    public static final CharSequence CLARA_ROOTFOLDER = ROOTFOLDER + "/" + CLARA;
    static final String YOUR_APP_NAME = "yourAppName";
    public static final String YOUR_URI = "yourSchema://example";
    @Test
    public void usage1() throws SharkException, IOException, ASAPException {
        SharkTestPeerFS.removeFolder(ALICE_ROOTFOLDER); // clean previous version before

        SharkPeerFS sPeer = new SharkPeerFS(ALICE, ALICE_ROOTFOLDER);
        YourComponentFactory factory = new YourComponentFactory();
        Class facadeClass = YourComponent.class;
        sPeer.addComponent(factory, facadeClass);

        sPeer.getComponent(YourComponent.class);
        sPeer.start();
    }

    @Test
    public void scratch() {
        System.out.println(DateTimeHelper.long2DateString(System.currentTimeMillis()));
        System.out.println(DateTimeHelper.long2ExactTimeString(System.currentTimeMillis()));
        System.out.println(DateFormat.getInstance().format(new Date(System.currentTimeMillis())));
    }

    @Test
    public void sendAMessage() throws SharkException, ASAPException, IOException, InterruptedException {
        ////////////// setup Alice
        SharkTestPeerFS.removeFolder(ALICE_ROOTFOLDER);
        SharkTestPeerFS aliceSharkPeer = new SharkTestPeerFS(ALICE, ALICE_ROOTFOLDER);
        YourComponent aliceComponent = TestHelper.setupComponent(aliceSharkPeer);
        ExampleYourComponentListener aliceListener = new ExampleYourComponentListener();
        aliceComponent.subscribeYourComponentListener(aliceListener);

        // Start alice peer
        aliceSharkPeer.start();

        ////////////// setup Bob
        SharkTestPeerFS.removeFolder(BOB_ROOTFOLDER);
        SharkTestPeerFS bobSharkPeer = new SharkTestPeerFS(BOB, BOB_ROOTFOLDER);
        YourComponent bobComponent = TestHelper.setupComponent(bobSharkPeer);
        ExampleYourComponentListener bobListener = new ExampleYourComponentListener();
        bobComponent.subscribeYourComponentListener(bobListener);

        // Start bob peer
        bobSharkPeer.start();

        // Bob sends a broadcast on format A
        bobComponent.sendBroadcastMessage(YOUR_URI, "Hi all listeners of A");

        ///////////////////////////////// Test specific code - make an encounter Alice Bob
        aliceSharkPeer.getASAPTestPeerFS().startEncounter(7777, bobSharkPeer.getASAPTestPeerFS());

        // give them moment to exchange data
        Thread.sleep(2000);
        //Thread.sleep(Long.MAX_VALUE);
        System.out.println("slept a moment");

        ////////////////////////////////// test if anything was ok.
        // Alice should have received Bob broadcast on format A but nothing on format B
        Assert.assertEquals(1, aliceListener.counter);
    }

    @Test
    public void hubDescriptions() throws SharkException, IOException {
        ////////////// setup Alice
        SharkTestPeerFS.removeFolder(ALICE_ROOTFOLDER);
        SharkTestPeerFS aliceSharkPeer = new SharkTestPeerFS(ALICE, ALICE_ROOTFOLDER);
        YourComponent aliceComponent = TestHelper.setupComponent(aliceSharkPeer);

        // Start alice peer
        aliceSharkPeer.start();

        aliceSharkPeer.addHubDescription(new TCPHubConnectorDescriptionImpl("exampleHost_A", 1234));
        aliceSharkPeer.addHubDescription(new TCPHubConnectorDescriptionImpl("exampleHost_B", 1235));
        aliceSharkPeer.addHubDescription(new TCPHubConnectorDescriptionImpl("exampleHost_C", 1265));

        // relaunch
        aliceSharkPeer = new SharkTestPeerFS(ALICE, ALICE_ROOTFOLDER);
        aliceComponent = TestHelper.setupComponent(aliceSharkPeer);

        aliceSharkPeer.start();

        aliceSharkPeer.getHubDescription(0);
        aliceSharkPeer.getHubDescription(1);
        aliceSharkPeer.getHubDescription(2);
    }

    @Test
    public void singleHub_twoPeers_multiChannel() throws SharkException, IOException, InterruptedException {
        int hubPort = TestHelper.getPortNumber();

        String aliceFolder = TestHelper.getUniqueFolderName(ALICE_ROOTFOLDER.toString());
        String bobFolder = TestHelper.getUniqueFolderName(BOB_ROOTFOLDER.toString());

        HubConnectorDescription localHostHubDescription =
                new TCPHubConnectorDescriptionImpl("localhost", hubPort, true);

        Collection<HubConnectorDescription> hubDescriptions = new ArrayList<>();
        hubDescriptions.add(localHostHubDescription);

        // launch asap hub
        ASAPTCPHub hub = ASAPTCPHub.startTCPHubThread(hubPort, true, TestHelper.MAX_IDLE_IN_SECONDS);

        // give it moment to settle in
        Thread.sleep(1000);

        //////////////////////////////// setup Alice
        SharkTestPeerFS.removeFolder(aliceFolder);
        SharkTestPeerFS aliceSharkPeer = new SharkTestPeerFS(ALICE, aliceFolder);
        YourComponent aliceComponent = TestHelper.setupComponent(aliceSharkPeer);
        // start alice peer
        aliceSharkPeer.start();

        // setup Bob
        ////////////// setup Bob
        SharkTestPeerFS.removeFolder(bobFolder);
        SharkTestPeerFS bobSharkPeer = new SharkTestPeerFS(BOB, bobFolder);
        YourComponent bobComponent = TestHelper.setupComponent(bobSharkPeer);
        ExampleYourComponentListener bobListener = new ExampleYourComponentListener();
        bobComponent.subscribeYourComponentListener(bobListener);

        // Start bob peer
        bobSharkPeer.start();

        // Alice sends a message
        aliceComponent.sendBroadcastMessage(YOUR_URI, "Hi there");

        ///////////////////// connect to hub - Alice
        // setup encounter manager with a connection handler
        ASAPEncounterManagerImpl aliceEncounterManager =
                new ASAPEncounterManagerImpl(aliceSharkPeer.getASAPTestPeerFS(),ALICE);

        // setup hub manager
        ASAPHubManager aliceHubManager = ASAPHubManagerImpl.createASAPHubManager(aliceEncounterManager);

        // connect with bulk import
        aliceHubManager.connectASAPHubs(hubDescriptions, aliceSharkPeer.getASAPPeer(), true);
        Thread.sleep(1000);

        ///////////////////// connect to hub - Bob
        // setup encounter manager with a connection handler
        ASAPEncounterManagerImpl bobEncounterManager =
                new ASAPEncounterManagerImpl(bobSharkPeer.getASAPTestPeerFS(), BOB);

        // setup hub manager
        ASAPHubManager bobHubManager = ASAPHubManagerImpl.createASAPHubManager(bobEncounterManager);

        // connect to hub - Bob
        bobHubManager.connectASAPHubs(hubDescriptions, bobSharkPeer.getASAPPeer(), true);
        Thread.sleep(1000);

        // give them moment to exchange data
        Thread.sleep(5000);
        //Thread.sleep(Long.MAX_VALUE);
        System.out.println("slept a moment");

        // Bob received a message?
        Assert.assertEquals(1, bobListener.counter);

        // shut down
        hub.kill();
        aliceHubManager.kill();
        bobHubManager.kill();
    }
}
