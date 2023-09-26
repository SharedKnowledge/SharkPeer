package net.sharksystem;

import net.sharksystem.asap.*;
import net.sharksystem.asap.apps.TCPServerSocketAcceptor;
import net.sharksystem.lsan.LSAN;
import net.sharksystem.lsan.LSANFactory;
import net.sharksystem.utils.streams.StreamPairImpl;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.Socket;

import static net.sharksystem.SharkComponentTests.*;

public class LargeScaleAdhocNetworkTests {
    /**
     * Setup two peer witth their encounter manager. Run an encounter.
     */
    @Test
    public void aliceMeetsBob() throws SharkException, ASAPException, IOException, InterruptedException {
        // setup
        String aliceFolder = TestHelper.getUniqueFolderName(ALICE_ROOTFOLDER.toString());
        String bobFolder = TestHelper.getUniqueFolderName(BOB_ROOTFOLDER.toString());

        //////////////////////////////// setup Alice
        SharkTestPeerFS.removeFolder(aliceFolder); // clean folder from previous tests

        // create shark peer instance to represent alice
        SharkPeerFS aliceSharkPeer = new SharkPeerFS(ALICE, aliceFolder);

        // create a shark component as an example
        YourComponent aliceComponent = TestHelper.setupComponent(aliceSharkPeer);

        // set large scale ad hoc network component
        aliceSharkPeer.addComponent(new LSANFactory(), LSAN.class);
        LSAN aliceLSAN = (LSAN) aliceSharkPeer.getComponent(LSAN.class);

        // now, create an ASAPPeer
        ASAPPeerFS aliceASAPPeerFS = new ASAPPeerFS(ALICE, aliceFolder, aliceSharkPeer.getSupportedFormats());

        // start alice shark peer
        aliceSharkPeer.start(aliceASAPPeerFS);

        /* now we have a running asap peer and - one or more Shark components running and
        waiting for incoming messages. No, lets set up an encounter manager on alice side.
         */
        ASAPEncounterManagerImpl aliceEncounterManager =
                new ASAPEncounterManagerImpl(aliceASAPPeerFS);

        /* system is up and running. Now, our LargeScaleNetwork component
        needs access to our encounter manager. Hand it over
         */
        aliceLSAN.addEncounterManagerAdmin(aliceEncounterManager);

        ////////////// setup Bob - same routine
        SharkTestPeerFS.removeFolder(bobFolder);
        SharkTestPeerFS bobSharkPeer = new SharkTestPeerFS(BOB, bobFolder);
        YourComponent bobComponent = TestHelper.setupComponent(bobSharkPeer);
        ExampleYourComponentListener bobListener = new ExampleYourComponentListener();
        bobComponent.subscribeYourComponentListener(bobListener);

        bobSharkPeer.addComponent(new LSANFactory(), LSAN.class);
        LSAN bobLSAN = (LSAN) bobSharkPeer.getComponent(LSAN.class);
        ASAPPeerFS bobASAPPeerFS = new ASAPPeerFS(BOB, bobFolder, bobSharkPeer.getSupportedFormats());
        bobSharkPeer.start(bobASAPPeerFS);
        ASAPEncounterManagerImpl bobEncounterManager = new ASAPEncounterManagerImpl(bobASAPPeerFS);
        bobLSAN.addEncounterManagerAdmin(bobEncounterManager);

        // Alice sends a message
        aliceComponent.sendBroadcastMessage(YOUR_URI, "Hi there");

        //// create an actual connection
        int alicePort = TestHelper.getPortNumber(); // in unit test always a good idea to choose a fresh port

        // offer a port on alice side
        new TCPServerSocketAcceptor(alicePort, aliceEncounterManager);
        // connect to it
        Socket connect2Alice = new Socket("localhost", alicePort);

        // handle to encounter manager on bob side
        bobEncounterManager.handleEncounter(
            StreamPairImpl.getStreamPair(
                connect2Alice.getInputStream(), connect2Alice.getOutputStream(), ALICE, ALICE),
                ASAPEncounterConnectionType.INTERNET);

        // give it a moment to exchange data
        Thread.sleep(500);
        //Thread.sleep(Long.MAX_VALUE);
        System.out.println("slept a moment");

        // Bob received a message?
        Assert.assertEquals(1, bobListener.counter);
    }
}
