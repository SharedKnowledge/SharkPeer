package net.sharksystem;

import net.sharksystem.asap.ASAPException;
import net.sharksystem.hub.HubConnectorDescription;
import net.sharksystem.hub.HubConnectorProtocol;
import net.sharksystem.hub.TCPHubConnectorDescription;
import net.sharksystem.hub.hubside.Hub;
import net.sharksystem.hub.hubside.TCPHub;
import org.junit.Test;

import java.io.IOException;

public class ASAPHubUsage extends TestHelper {
    public static final int MAX_HUB_CONNECTION_IDLE_IN_SECONDS = 1;
    public ASAPHubUsage() {
        super(ASAPHubUsage.class.getSimpleName());
    }

    @Test
    public void usage() throws SharkException, ASAPException, IOException, InterruptedException {
        this.setUpScenario_1();

        // Alice broadcast message in channel URI - not signed, not encrypted
        this.aliceComponent.sendBroadcastMessageA(URI, MESSAGE);

        // setup ASAP hub
        TCPHub hub = new TCPHub(Hub.DEFAULT_PORT);
        hub.setPortRange(7000, 9000); // optional - required to configure a firewall
        hub.setMaxIdleConnectionInSeconds(MAX_HUB_CONNECTION_IDLE_IN_SECONDS);
        new Thread(hub).start();

        // define hub connector description for this hub
        HubConnectorDescription hubDescription = new TCPHubConnectorDescription() {
            public String getHubHostName() {
                return "localhost";
            }
            public int getHubHostPort() {
                return Hub.DEFAULT_PORT;
            }
            public HubConnectorProtocol getHubConnectorType() {
                return HubConnectorProtocol.TCP;
            }
        };

        // set hub description on Alice side
        this.alicePeer.addASAPHub(hubDescription);
        // .. and Bob side
        this.bobPeer.addASAPHub(hubDescription); // same hub - same description

        this.alicePeer.connectASAPHubs();
        this.bobPeer.connectASAPHubs(HubConnectorProtocol.TCP); // variant .. specify connector type.
        this.alicePeer.connectASAPHub(hubDescription); // variant

        this.alicePeer.disconnectASAPHubs();
        this.alicePeer.disconnectASAPHub(hubDescription); // variant
        this.bobPeer.disconnectASAPHubs(HubConnectorProtocol.TCP); // variant .. specify connector type.

        this.alicePeer.removeASAPHub(hubDescription);
        this.bobPeer.removeASAPHubs(HubConnectorProtocol.TCP); // variant
        this.bobPeer.removeASAPHubs(); // variant

        /*
        // test results on Bobs side
        ASAPStorage bobAsapStorage = bobMessengerImpl.getASAPStorage();
        List<CharSequence> senderList = bobAsapStorage.getSender();

        // Bob knows Alice now.
        Assert.assertNotNull(senderList);
        Assert.assertFalse(senderList.isEmpty());
        CharSequence senderID = senderList.get(0);
        Assert.assertTrue(alicePeer.samePeer(senderID));

        // Bob received message
        ASAPStorage senderIncomingStorage = bobAsapStorage.getExistingIncomingStorage(senderID);
        ASAPChannel channel = senderIncomingStorage.getChannel(URI);
        byte[] message = channel.getMessages().getMessage(0, true);
        Assert.assertNotNull(message);

        SharkMessengerChannel bobChannel = bobMessenger.getChannel(URI);
        SharkMessage sharkMessage = bobChannel.getMessages().getSharkMessage(0, true);
        // message received by Bob from Alice?
        Assert.assertTrue(alicePeer.samePeer(sharkMessage.getSender()));
        Assert.assertTrue(Utils.compareArrays(sharkMessage.getContent(), MESSAGE_BYTE));
        Assert.assertFalse(sharkMessage.encrypted());
        Assert.assertFalse(sharkMessage.verified());
         */

        /*
        ///////////////////////////////// Encounter Alice - Clara ////////////////////////////////////////////////////
        this.runEncounter(this.alicePeer, this.claraPeer, true);

        // test results
        // message received by Clara from Alice?
        Assert.assertTrue(alicePeer.samePeer(sharkMessage.getSender()));
        Assert.assertTrue(Utils.compareArrays(sharkMessage.getContent(), MESSAGE_BYTE));
        Assert.assertFalse(sharkMessage.encrypted());
        Assert.assertFalse(sharkMessage.verified());
        */

    }
}
