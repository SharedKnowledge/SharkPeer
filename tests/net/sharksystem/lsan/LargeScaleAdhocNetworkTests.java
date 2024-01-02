package net.sharksystem.lsan;

import net.sharksystem.*;
import net.sharksystem.asap.*;
import net.sharksystem.asap.apps.TCPServerSocketAcceptor;
import net.sharksystem.utils.streams.StreamPairImpl;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import static net.sharksystem.SharkComponentTests.*;
import static net.sharksystem.utils.testsupport.TestConstants.*;

public class LargeScaleAdhocNetworkTests {
    public static YourComponent aliceComponent;
    private YourComponent bobComponent;
    private YourComponent claraComponent;

    private YourComponent daveComponent;
    private LSAN aliceLSAN;

    private LSAN bobLSAN;

    private LSAN claraLSAN;

    private LSAN daveLSAN;
    private ASAPEncounterManagerImpl aliceEncounterManager;
    private ASAPEncounterManagerImpl bobEncounterManager;

    private ASAPEncounterManagerImpl claraEncounterManager;

    private ASAPEncounterManagerImpl daveEncounterManager;
    private ExampleYourComponentListener bobListener;

    private ExampleYourComponentListener claraListener;

    private ExampleYourComponentListener daveListener;

    private ExampleYourComponentListener aliceListener;

    public static final String TESTFOLDER_NAME = "lsanTests/";

//    public void setupPeer(String name, CharSequence rootFolder, String peerId, YourComponent component, LSAN lsan, CharSequence asapName, ASAPEncounterManagerImpl em) throws SharkException, IOException, InterruptedException {
//        String peerFolder = TestHelper.getUniqueFolderName(rootFolder.toString());
//
//        //////////////////////////////// setup Peer
//        SharkTestPeerFS.removeFolder(peerFolder); // clean folder from previous tests
//
//        // create shark peer instance to represent this peer
//        SharkPeerFS sharkPeer = new SharkPeerFS(peerId, peerFolder);
//
//        // create a shark component as an example
//        component = TestHelper.setupComponent(sharkPeer);
//
//        // set large scale ad hoc network component
//        sharkPeer.addComponent(new LSANFactory(), LSAN.class);
//        lsan = (LSAN) sharkPeer.getComponent(LSAN.class);
//
//        // now, create an ASAPPeer - take supported format from Shark peer
//        ASAPPeerFS ASAPPeerFS = new ASAPPeerFS(asapName, peerFolder, sharkPeer.getSupportedFormats());
//
//        // start alice shark peer
//        sharkPeer.start(ASAPPeerFS);
//
//        /* now we have a running asap peer and - one or more Shark components running and
//        waiting for incoming messages. No, lets set up an encounter manager on alice side.
//         */
//        em = new ASAPEncounterManagerImpl(ASAPPeerFS, peerId);
//
//        /* system is up and running. Now, our LargeScaleNetwork component
//        needs access to our encounter manager. Hand it over
//         */
//        aliceLSAN.addEncounterManagerAdmin(aliceEncounterManager);
//    }

//    public void setupListener(ExampleYourComponentListener listener, YourComponent component){
//        ////////////// Setup Listener
//        listener = new ExampleYourComponentListener();
//        component.subscribeYourComponentListener(listener);
//    }
    public void setup() throws SharkException, IOException, InterruptedException {
        // setup

//        setupPeer("alice", ALICE_ROOTFOLDER, ALICE_ID, aliceComponent, aliceLSAN, ALICE, aliceEncounterManager);
//        setupListener(aliceListener, aliceComponent);
//
//        setupPeer("bob", BOB_ROOTFOLDER, BOB_ID, bobComponent, bobLSAN, BOB, bobEncounterManager);
//        setupListener(bobListener, bobComponent);
//
//        setupPeer("clara", CLARA_ROOTFOLDER, CLARA_ID, claraComponent, claraLSAN, CLARA, claraEncounterManager);
//        setupListener(claraListener, claraComponent);
//
//        setupPeer("dave", DAVE_ROOTFOLDER, DAVID_ID, daveComponent, daveLSAN, DAVE, daveEncounterManager);
//        setupListener(daveListener, daveComponent);

        String aliceFolder = TestHelper.getUniqueFolderName(ALICE_ROOTFOLDER.toString());
        String bobFolder = TestHelper.getUniqueFolderName(BOB_ROOTFOLDER.toString());
        String claraFolder = TestHelper.getUniqueFolderName(CLARA_ROOTFOLDER.toString());
        String daveFolder = TestHelper.getUniqueFolderName(DAVE_ROOTFOLDER.toString());

        net.sharksystem.utils.testsupport.TestHelper.incrementTestNumber();

        //////////////////////////////// setup Alice
        SharkTestPeerFS.removeFolder(aliceFolder); // clean folder from previous tests

        // create shark peer instance to represent alice
        SharkTestPeerFS aliceSharkPeer = new SharkTestPeerFS(ALICE_ID, aliceFolder);

        // create a shark component as an example
        aliceComponent = TestHelper.setupComponent(aliceSharkPeer);

        ////////////// Setup Alice Listener
        aliceListener = new ExampleYourComponentListener();
        aliceComponent.subscribeYourComponentListener(aliceListener);

        // set large scale ad hoc network component
        aliceSharkPeer.addComponent(new LSANFactory(), LSAN.class);
        aliceLSAN = (LSAN) aliceSharkPeer.getComponent(LSAN.class);

        // now, create an ASAPPeer - take supported format from Shark peer
        ASAPPeerFS aliceASAPPeerFS = new ASAPPeerFS(ALICE, aliceFolder, aliceSharkPeer.getSupportedFormats());

        // start alice shark peer
        aliceSharkPeer.start(aliceASAPPeerFS);

        /* now we have a running asap peer and - one or more Shark components running and
        waiting for incoming messages. No, lets set up an encounter manager on alice side.
         */
        aliceEncounterManager = new ASAPEncounterManagerImpl(aliceASAPPeerFS, ALICE_ID);

        /* system is up and running. Now, our LargeScaleNetwork component
        needs access to our encounter manager. Hand it over
         */
        aliceLSAN.addEncounterManagerAdmin(aliceEncounterManager);

        ////////////// setup Bob - same routine
        SharkTestPeerFS.removeFolder(bobFolder);
        SharkTestPeerFS bobSharkPeer = new SharkTestPeerFS(BOB_ID, bobFolder);
        bobComponent = TestHelper.setupComponent(bobSharkPeer);
        bobListener = new ExampleYourComponentListener();
        bobComponent.subscribeYourComponentListener(bobListener);

        bobSharkPeer.addComponent(new LSANFactory(), LSAN.class);
        bobLSAN = (LSAN) bobSharkPeer.getComponent(LSAN.class);
        ASAPPeerFS bobASAPPeerFS = new ASAPPeerFS(BOB_ID, bobFolder, bobSharkPeer.getSupportedFormats());
        bobSharkPeer.start(bobASAPPeerFS);
        bobEncounterManager = new ASAPEncounterManagerImpl(bobASAPPeerFS, BOB_ID);
        bobLSAN.addEncounterManagerAdmin(bobEncounterManager);


        /////////////////////////--------------- setup Clara -----------------////////////////////////////////


        SharkTestPeerFS.removeFolder(claraFolder);
        SharkTestPeerFS claraSharkPeer = new SharkTestPeerFS(CLARA_ID, claraFolder);
        claraComponent = TestHelper.setupComponent(claraSharkPeer);

        ////////////// Setup Clara Listener
        claraListener = new ExampleYourComponentListener();
        claraComponent.subscribeYourComponentListener(claraListener);

        ////////////// set large scale ad hoc network component for Clara
        claraSharkPeer.addComponent(new LSANFactory(), LSAN.class);
        claraLSAN = (LSAN) claraSharkPeer.getComponent(LSAN.class);

        ////////////// Create an ASAPPeer for Clara - take supported format from Shark peer
        ASAPPeerFS claraASAPPeerFS = new ASAPPeerFS(CLARA_ID, claraFolder, claraSharkPeer.getSupportedFormats());
        ////////////// start Clara shark peer
        claraSharkPeer.start(claraASAPPeerFS);

        ////////////// Setup Clara Encounter Manager
        claraEncounterManager = new ASAPEncounterManagerImpl(claraASAPPeerFS, CLARA_ID);
        claraLSAN.addEncounterManagerAdmin(claraEncounterManager);

        /////////////////////////--------------- setup Dave -----------------////////////////////////////////


        SharkTestPeerFS.removeFolder(daveFolder);
        SharkTestPeerFS daveSharkPeer = new SharkTestPeerFS(DAVID_ID, daveFolder);
        daveComponent = TestHelper.setupComponent(daveSharkPeer);

        ////////////// Setup Dave Listener
        daveListener = new ExampleYourComponentListener();
        daveComponent.subscribeYourComponentListener(daveListener);

        ////////////// set large scale ad hoc network component for Dave
        daveSharkPeer.addComponent(new LSANFactory(), LSAN.class);
        daveLSAN = (LSAN) daveSharkPeer.getComponent(LSAN.class);

        ////////////// Create an ASAPPeer for Dave - take supported format from Shark peer
        ASAPPeerFS daveASAPPeerFS = new ASAPPeerFS(DAVID_ID, daveFolder, daveSharkPeer.getSupportedFormats());
        ////////////// start Dave shark peer
        daveSharkPeer.start(daveASAPPeerFS);

        ////////////// Setup Dave Encounter Manager
        daveEncounterManager = new ASAPEncounterManagerImpl(daveASAPPeerFS, DAVID_ID);
        daveLSAN.addEncounterManagerAdmin(daveEncounterManager);
    }
    /**
     * Setup two peer with their encounter manager. Run an encounter.
     */
    public void connectPeers(ASAPEncounterManagerImpl em1, ASAPEncounterManagerImpl em2, String peerId) throws SharkException, IOException, InterruptedException {
        int em1Port = TestHelper.getPortNumber();

        new TCPServerSocketAcceptor(em1Port, em1);
        Socket connect2Em1 = new Socket("localhost", em1Port);

        em2.handleEncounter(
                StreamPairImpl.getStreamPair(
                        connect2Em1.getInputStream(), connect2Em1.getOutputStream(), peerId, peerId),
                ASAPEncounterConnectionType.INTERNET);
    }

    @Test
    public void aliceSendsBroadcast() throws SharkException, ASAPException, IOException, InterruptedException {
        this.setup();

        // Alice sends a message
        aliceComponent.sendBroadcastMessage(YOUR_URI, "Hi there");

        // connect A and B
        connectPeers(aliceEncounterManager, bobEncounterManager, ALICE_ID);
        Thread.sleep(100);
        // connect B and C
        connectPeers(bobEncounterManager, claraEncounterManager, BOB_ID);
        Thread.sleep(100);
        // connect C and D
        connectPeers(claraEncounterManager, daveEncounterManager, CLARA_ID);
        Thread.sleep(100);
        // connect A and D  **(Cycle connection)**
        connectPeers(aliceEncounterManager, daveEncounterManager, ALICE_ID);


        // give it a moment to exchange data
        Thread.sleep(500);
        //Thread.sleep(Long.MAX_VALUE);
        System.out.println("slept a moment");
        System.out.println(aliceEncounterManager.getConnectedPeerIDs());

        // Bob received a message?
        Assert.assertEquals(1, bobListener.counter);
        // Clara received a message?
        Assert.assertEquals(1, claraListener.counter);

        Assert.assertEquals(1, daveListener.counter);

        Assert.assertEquals(0, aliceListener.counter);
    }
}
