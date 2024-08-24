package net.sharksystem;

import net.sharksystem.asap.*;
import net.sharksystem.asap.apps.TCPServerSocketAcceptor;
import net.sharksystem.asap.utils.DateTimeHelper;
import net.sharksystem.hub.hubside.ASAPTCPHub;
import net.sharksystem.hub.peerside.ASAPHubManager;
import net.sharksystem.hub.peerside.ASAPHubManagerImpl;
import net.sharksystem.hub.peerside.HubConnectorDescription;
import net.sharksystem.hub.peerside.TCPHubConnectorDescriptionImpl;
import java.net.Socket;

import net.sharksystem.testhelper.SharkPeerTestHelper;
import org.junit.Assert;
import org.junit.Test;

import net.sharksystem.utils.streams.StreamPairImpl;


import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class SharkComponentTests {
    public static final CharSequence ALICE_ID = "Alice";
    public static final CharSequence BOB_ID = "Bob";

    public static final CharSequence CLARA = "Clara";

    public static final CharSequence DAVE = "Dave";
    static final CharSequence ROOTFOLDER = "sharkComponent";
    public static final CharSequence ALICE_ROOTFOLDER = ROOTFOLDER + "/" + ALICE_ID;
    public static final CharSequence BOB_ROOTFOLDER = ROOTFOLDER + "/" + BOB_ID;
    public static final CharSequence CLARA_ROOTFOLDER = ROOTFOLDER + "/" + CLARA;

    public static final CharSequence DAVE_ROOTFOLDER = ROOTFOLDER + "/" + DAVE;

    static final String YOUR_APP_NAME = "yourAppName";
    public static final String YOUR_URI = "yourSchema://example";
    @Test
    public void usage1() throws SharkException, IOException, ASAPException {
        SharkTestPeerFS.removeFolder(ALICE_ROOTFOLDER); // clean previous version before

        SharkPeerFS sPeer = new SharkPeerFS(ALICE_ROOTFOLDER);
        YourComponentFactory factory = new YourComponentFactory();
        Class facadeClass = YourComponent.class;
        sPeer.addComponent(factory, facadeClass);

        sPeer.getComponent(YourComponent.class);
        sPeer.start(ALICE_ID);
    }

    @Test
    public void scratch() {
        System.out.println(DateTimeHelper.long2DateString(System.currentTimeMillis()));
        System.out.println(DateTimeHelper.long2ExactTimeString(System.currentTimeMillis()));
        System.out.println(DateFormat.getInstance().format(new Date(System.currentTimeMillis())));
    }

    SharkTestPeerFS aliceSharkPeer, bobSharkPeer;
    YourComponent aliceComponent, bobComponent;
    ExampleYourComponentListener aliceListener, bobListener;

    private void setUpAndStartAliceAndBob() throws SharkException {
        ////////////// setup Alice
        String folderName = SharkPeerTestHelper.getUniqueFolderName(ALICE_ROOTFOLDER.toString());
        SharkTestPeerFS.removeFolder(folderName);

        System.out.println("alice uses folder " + folderName);
        aliceSharkPeer = new SharkTestPeerFS(folderName);
        aliceComponent = TestHelper.setupComponent(aliceSharkPeer);
        aliceListener = new ExampleYourComponentListener();
        aliceComponent.subscribeYourComponentListener(aliceListener);

        // Start alice peer
        aliceSharkPeer.start(ALICE_ID);

        ////////////// setup Bob
        folderName = SharkPeerTestHelper.getUniqueFolderName(BOB_ROOTFOLDER.toString());
        SharkTestPeerFS.removeFolder(folderName);
        System.out.println("bob uses folder " + folderName);
        bobSharkPeer = new SharkTestPeerFS(folderName);
        bobComponent = TestHelper.setupComponent(bobSharkPeer);
        bobListener = new ExampleYourComponentListener();
        bobComponent.subscribeYourComponentListener(bobListener);

        // Start bob peer
        bobSharkPeer.start(BOB_ID);

    }

    @Test
    public void sendAMessage() throws SharkException, ASAPException, IOException, InterruptedException {
        this.setUpAndStartAliceAndBob();
        // Bob sends a broadcast on format A
        bobComponent.sendBroadcastMessage(YOUR_URI, "Hi all listeners of A");

        ///////////////////////////////// Test specific code - make an encounter Alice Bob
        aliceSharkPeer.getASAPTestPeerFS().startEncounter(TestHelper.getPortNumber(), bobSharkPeer.getASAPTestPeerFS());

        // give them moment to exchange data
        Thread.sleep(200);
        //Thread.sleep(Long.MAX_VALUE);
        System.out.println("slept a moment");

        ////////////////////////////////// test if anything was ok.
        // Alice should have received Bob broadcast on format A but nothing on format B
        Assert.assertEquals(1, aliceListener.counter);
    }

    @Test
    public void sendAMessageUseEncounterManager() throws SharkException, ASAPException, IOException, InterruptedException {
        this.setUpAndStartAliceAndBob();
        // Bob sends a broadcast on format A
        bobComponent.sendBroadcastMessage(YOUR_URI, "Hi all listeners of A");

        ///////////////////////////////// setup with encounter manager
        ASAPConnectionHandler aliceConnectionHandler = (ASAPConnectionHandler) aliceSharkPeer.getASAPPeer();
        ASAPEncounterManager aliceEncounterManager =
                new ASAPEncounterManagerImpl(aliceConnectionHandler, aliceSharkPeer.getPeerID());

        ASAPConnectionHandler bobConnectionHandler = (ASAPConnectionHandler) bobSharkPeer.getASAPPeer();
        ASAPEncounterManager bobEncounterManager =
                new ASAPEncounterManagerImpl(bobConnectionHandler, bobSharkPeer.getPeerID());

        ////////////////////////// set up server socket and handle connection requests
        int portNumberAlice = TestHelper.getPortNumber();
        TCPServerSocketAcceptor aliceTcpServerSocketAcceptor =
                new TCPServerSocketAcceptor(portNumberAlice, aliceEncounterManager);

        int portNumberBob = TestHelper.getPortNumber();
        TCPServerSocketAcceptor bobTcpServerSocketAcceptor =
                new TCPServerSocketAcceptor(portNumberBob, bobEncounterManager);

        // give it a moment to settle
        Thread.sleep(5);

        // now, both side wait for connection establishment. Example

        // open connection to Bob
        Socket socket = new Socket("localhost", portNumberBob);

        aliceEncounterManager.handleEncounter(
                StreamPairImpl.getStreamPair(socket.getInputStream(), socket.getOutputStream()),
                ASAPEncounterConnectionType.INTERNET);

        // give them moment to exchange data
        Thread.sleep(200);
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
        SharkTestPeerFS aliceSharkPeer = new SharkTestPeerFS(ALICE_ROOTFOLDER);
        YourComponent aliceComponent = TestHelper.setupComponent(aliceSharkPeer);

        // Start alice peer
        aliceSharkPeer.start(ALICE_ID);

        aliceSharkPeer.addHubDescription(new TCPHubConnectorDescriptionImpl("exampleHost_A", 1234));
        aliceSharkPeer.addHubDescription(new TCPHubConnectorDescriptionImpl("exampleHost_B", 1235));
        aliceSharkPeer.addHubDescription(new TCPHubConnectorDescriptionImpl("exampleHost_C", 1265));

        // relaunch
        aliceSharkPeer = new SharkTestPeerFS(ALICE_ROOTFOLDER);
        aliceComponent = TestHelper.setupComponent(aliceSharkPeer);

        aliceSharkPeer.start(ALICE_ID);

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
        SharkTestPeerFS aliceSharkPeer = new SharkTestPeerFS(aliceFolder);
        YourComponent aliceComponent = TestHelper.setupComponent(aliceSharkPeer);
        // start alice peer
        aliceSharkPeer.start(ALICE_ID);

        // setup Bob
        ////////////// setup Bob
        SharkTestPeerFS.removeFolder(bobFolder);
        SharkTestPeerFS bobSharkPeer = new SharkTestPeerFS(bobFolder);
        YourComponent bobComponent = TestHelper.setupComponent(bobSharkPeer);
        ExampleYourComponentListener bobListener = new ExampleYourComponentListener();
        bobComponent.subscribeYourComponentListener(bobListener);

        // Start bob peer
        bobSharkPeer.start(BOB_ID);

        // Alice sends a message
        aliceComponent.sendBroadcastMessage(YOUR_URI, "Hi there");

        ///////////////////// connect to hub - Alice
        // setup encounter manager with a connection handler
        ASAPEncounterManagerImpl aliceEncounterManager =
                new ASAPEncounterManagerImpl(aliceSharkPeer.getASAPTestPeerFS(), ALICE_ID);

        // setup hub manager
        ASAPHubManager aliceHubManager = ASAPHubManagerImpl.createASAPHubManager(aliceEncounterManager);

        // connect with bulk import
        aliceHubManager.connectASAPHubs(hubDescriptions, aliceSharkPeer.getASAPPeer(), true);
        Thread.sleep(1000);

        ///////////////////// connect to hub - Bob
        // setup encounter manager with a connection handler
        ASAPEncounterManagerImpl bobEncounterManager =

                new ASAPEncounterManagerImpl(bobSharkPeer.getASAPTestPeerFS(), BOB_ID);

                new ASAPEncounterManagerImpl(bobSharkPeer.getASAPTestPeerFS(), BOB_ID);


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
