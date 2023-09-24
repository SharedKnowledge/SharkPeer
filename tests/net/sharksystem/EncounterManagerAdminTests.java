package net.sharksystem;

import net.sharksystem.asap.ASAPEncounterManagerImpl;
import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.EncounterConnectionType;
import net.sharksystem.asap.apps.TCPServerSocketAcceptor;
import net.sharksystem.hub.hubside.ASAPTCPHub;
import net.sharksystem.hub.peerside.ASAPHubManager;
import net.sharksystem.hub.peerside.ASAPHubManagerImpl;
import net.sharksystem.hub.peerside.HubConnectorDescription;
import net.sharksystem.hub.peerside.TCPHubConnectorDescriptionImpl;
import net.sharksystem.utils.streams.StreamPairImpl;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;

import static net.sharksystem.SharkComponentTests.*;

public class EncounterManagerAdminTests {
    /**
     * Setup two peer witth their encounter manager. Run an encounter.
     */
    @Test
    public void sendAMessage() throws SharkException, ASAPException, IOException, InterruptedException {
        // setup
        String aliceFolder = TestHelper.getUniqueFolderName(ALICE_ROOTFOLDER.toString());
        String bobFolder = TestHelper.getUniqueFolderName(BOB_ROOTFOLDER.toString());

        //////////////////////////////// setup Alice
        SharkTestPeerFS.removeFolder(aliceFolder);
        SharkTestPeerFS aliceSharkPeer = new SharkTestPeerFS(ALICE, aliceFolder);
        YourComponent aliceComponent = TestHelper.setupComponent(aliceSharkPeer);
        // start alice peer

        // setup encounter manager with a connection handler
        ASAPEncounterManagerImpl aliceEncounterManager =
                new ASAPEncounterManagerImpl(aliceSharkPeer.getASAPTestPeerFS());

        // start alice peer including her encounter manager
        aliceSharkPeer.start(aliceEncounterManager, aliceSharkPeer.getASAPPeer());

        // setup Bob
        ////////////// setup Bob
        SharkTestPeerFS.removeFolder(bobFolder);
        SharkTestPeerFS bobSharkPeer = new SharkTestPeerFS(BOB, bobFolder);
        YourComponent bobComponent = TestHelper.setupComponent(bobSharkPeer);
        ExampleYourComponentListener bobListener = new ExampleYourComponentListener();
        bobComponent.subscribeYourComponentListener(bobListener);

        // setup encounter manager with a connection handler
        ASAPEncounterManagerImpl bobEncounterManager =
                new ASAPEncounterManagerImpl(bobSharkPeer.getASAPTestPeerFS());

        // Start bob peer
        bobSharkPeer.start(bobEncounterManager, bobSharkPeer.getASAPPeer());

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
                connect2Alice.getInputStream(), connect2Alice.getOutputStream(), null, ALICE),
                EncounterConnectionType.INTERNET);

        // give it a moment to exchange data
        Thread.sleep(500);
        //Thread.sleep(Long.MAX_VALUE);
        System.out.println("slept a moment");

        // Bob received a message?
        Assert.assertEquals(1, bobListener.counter);
    }
}
