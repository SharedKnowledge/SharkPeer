package net.sharksystem.lsan;

import net.sharksystem.*;
import net.sharksystem.asap.*;
import net.sharksystem.asap.apps.TCPServerSocketAcceptor;
import net.sharksystem.utils.streams.StreamPairImpl;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.Socket;

import static net.sharksystem.SharkComponentTests.*;
import static net.sharksystem.utils.testsupport.TestConstants.ALICE_ID;
import static net.sharksystem.utils.testsupport.TestConstants.BOB_ID;
import static net.sharksystem.utils.testsupport.TestConstants.CLARA_ID;

public class LargeScaleAdhocNetworkTests {
    public static YourComponent aliceComponent;
    private YourComponent bobComponent;

    private YourComponent claraComponent;
    private LSAN aliceLSAN;
    private LSAN bobLSAN;

    private LSAN claraLSAN;
    private ASAPEncounterManagerImpl aliceEncounterManager;
    private ASAPEncounterManagerImpl bobEncounterManager;

    private ASAPEncounterManagerImpl claraEncounterManager;
    private ExampleYourComponentListener bobListener;

    private ExampleYourComponentListener claraListener;

    public static final String TESTFOLDER_NAME = "lsanTests/";


    public void setup() throws SharkException, IOException, InterruptedException {
        // setup
        String aliceFolder = TestHelper.getUniqueFolderName(ALICE_ROOTFOLDER.toString());
        String bobFolder = TestHelper.getUniqueFolderName(BOB_ROOTFOLDER.toString());
        String claraFolder = TestHelper.getUniqueFolderName(CLARA_ROOTFOLDER.toString());

        net.sharksystem.utils.testsupport.TestHelper.incrementTestNumber();

        //////////////////////////////// setup Alice
        SharkTestPeerFS.removeFolder(aliceFolder); // clean folder from previous tests

        // create shark peer instance to represent alice
        SharkPeerFS aliceSharkPeer = new SharkPeerFS(ALICE_ID, aliceFolder);

        // create a shark component as an example
        aliceComponent = TestHelper.setupComponent(aliceSharkPeer);

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
    }
    /**
     * Setup two peer with their encounter manager. Run an encounter.
     */
    @Test
    public void aliceSendsBroadcast() throws SharkException, ASAPException, IOException, InterruptedException {
        this.setup();

        // Alice sends a message
        aliceComponent.sendBroadcastMessage(YOUR_URI, "Hi there");

        //// create an actual connection
        int alicePort = TestHelper.getPortNumber(); // in unit test always a good idea to choose a fresh port

        // offer a port on alice side
        new TCPServerSocketAcceptor(alicePort, aliceEncounterManager);
        Socket connect2Alice = new Socket("localhost", alicePort);

        // handle to encounter manager on bob side
        bobEncounterManager.handleEncounter(
            StreamPairImpl.getStreamPair(
                connect2Alice.getInputStream(), connect2Alice.getOutputStream(), ALICE_ID, ALICE_ID),
                ASAPEncounterConnectionType.INTERNET);

        // connect B and C
        Thread.sleep(100);
        int bobPort = TestHelper.getPortNumber();
        new TCPServerSocketAcceptor(bobPort, bobEncounterManager);
        Socket connect2Bob= new Socket("localhost", bobPort);
        claraEncounterManager.handleEncounter(
                StreamPairImpl.getStreamPair(
                        connect2Bob.getInputStream(), connect2Bob.getOutputStream(), BOB_ID, BOB_ID),
                ASAPEncounterConnectionType.INTERNET);

        // give it a moment to exchange data
        Thread.sleep(500);
        //Thread.sleep(Long.MAX_VALUE);
        System.out.println("slept a moment");

        // Bob received a message?
        Assert.assertEquals(1, bobListener.counter);
        // Clara received a message?
        Assert.assertEquals(1, claraListener.counter);
    }
}
