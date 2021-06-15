package net.sharksystem;

import net.sharksystem.asap.ASAPException;
import net.sharksystem.hub.HubConnectorAlgebra;
import net.sharksystem.hub.HubConnectorDescription;
import net.sharksystem.hub.HubConnectorProtocol;
import net.sharksystem.hub.TCPHubConnectorDescription;
import net.sharksystem.hub.hubside.Hub;
import net.sharksystem.hub.hubside.TCPHub;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ASAPHubUsage extends TestHelper {
    public static final int MAX_HUB_CONNECTION_IDLE_IN_SECONDS = 1;
    public ASAPHubUsage() {
        super(ASAPHubUsage.class.getSimpleName());
    }

    @Test
    public void hubDescriptionPersistence1() throws IOException, ASAPException {
        // serializing hub descriptions

        TCPHubConnectorDescription hubDescription1 = new TCPHubConnectorDescription() {
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

        TCPHubConnectorDescription hubDescription2 = new TCPHubConnectorDescription() {
            public String getHubHostName() {
                return "123.45.67.89";
            }
            public int getHubHostPort() {
                return 5555;
            }
            public HubConnectorProtocol getHubConnectorType() {
                return HubConnectorProtocol.TCP;
            }
        };

        byte[] serialize1 = HubConnectorAlgebra.serialize(hubDescription1);
        byte[] serialize2 = HubConnectorAlgebra.serialize(hubDescription2);

        HubConnectorDescription hubR1 = HubConnectorAlgebra.deserialize(serialize1);
        HubConnectorDescription hubR2 = HubConnectorAlgebra.deserialize(serialize2);

        Assert.assertTrue(hubR1.getHubConnectorType() == hubDescription1.getHubConnectorType());
        TCPHubConnectorDescription hubTCP_R1 = (TCPHubConnectorDescription) hubR1;
        Assert.assertTrue(hubTCP_R1.getHubHostName().equalsIgnoreCase(hubDescription1.getHubHostName()));
        Assert.assertTrue(hubTCP_R1.getHubHostPort() == hubDescription1.getHubHostPort());

        Assert.assertTrue(hubR2.getHubConnectorType() == hubDescription2.getHubConnectorType());
        TCPHubConnectorDescription hubTCP_R2 = (TCPHubConnectorDescription) hubR2;
        Assert.assertTrue(hubTCP_R2.getHubHostName().equalsIgnoreCase(hubDescription2.getHubHostName()));
        Assert.assertTrue(hubTCP_R2.getHubHostPort() == hubDescription2.getHubHostPort());

        // now all together
        List<HubConnectorDescription> descriptionList = new ArrayList<>();
        descriptionList.add(hubDescription1);
        descriptionList.add(hubDescription2);
        byte[] serializeCollection = HubConnectorAlgebra.serializeCollection(descriptionList);
        List<HubConnectorDescription> hubConnectorDescriptions = HubConnectorAlgebra.deserializeList(serializeCollection);

        Assert.assertEquals(2, hubConnectorDescriptions.size());
    }

    @Test
    public void hubDescriptionPersistence2() throws SharkException, ASAPException, IOException, InterruptedException {
        /*
        String aliceFolderName = aliceFolder + "_HubPersistence2";
        SharkTestPeerFS.removeFolder(aliceFolderName);
        this.alicePeer = new SharkTestPeerFS(ALICE_ID, aliceFolderName);
         */
        this.setUpScenario_1();

        TCPHubConnectorDescription hubDescription1 = new TCPHubConnectorDescription() {
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

        TCPHubConnectorDescription hubDescription2 = new TCPHubConnectorDescription() {
            public String getHubHostName() {
                return "123.45.67.89";
            }
            public int getHubHostPort() {
                return 5555;
            }
            public HubConnectorProtocol getHubConnectorType() {
                return HubConnectorProtocol.TCP;
            }
        };


        this.alicePeer.getASAPHubManager().addASAPHub(hubDescription1);
        this.alicePeer.getASAPHubManager().addASAPHub(hubDescription2);

        Collection<HubConnectorDescription> hubs = this.alicePeer.getASAPHubManager().getHubs();
        Assert.assertEquals(2, hubs.size());

        this.alicePeer.getASAPHubManager().removeASAPHub(hubDescription1);
        hubs = this.alicePeer.getASAPHubManager().getHubs();
        Assert.assertEquals(1, hubs.size());

        this.alicePeer.getASAPHubManager().removeASAPHub(hubDescription2);
        hubs = this.alicePeer.getASAPHubManager().getHubs();
        Assert.assertEquals(0, hubs.size());

        // add again
        this.alicePeer.getASAPHubManager().addASAPHub(hubDescription1);
        this.alicePeer.getASAPHubManager().addASAPHub(hubDescription2);

        // remove all TCP
        this.alicePeer.getASAPHubManager().removeASAPHubs(HubConnectorProtocol.TCP);
        hubs = this.alicePeer.getASAPHubManager().getHubs();
        Assert.assertEquals(0, hubs.size());

        // add again
        this.alicePeer.getASAPHubManager().addASAPHub(hubDescription1);
        this.alicePeer.getASAPHubManager().addASAPHub(hubDescription2);

        // remove all
        this.alicePeer.getASAPHubManager().removeASAPHubs();
        hubs = this.alicePeer.getASAPHubManager().getHubs();
        Assert.assertEquals(0, hubs.size());
    }

    @Test
    public void hubScenario1() throws SharkException, ASAPException, IOException, InterruptedException {
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
        this.alicePeer.getASAPHubManager().addASAPHub(hubDescription);
        this.alicePeer.getASAPHubManager().setReconnectIntervalInSeconds(1);
        // .. and Bob side
        this.bobPeer.getASAPHubManager().addASAPHub(hubDescription); // same hub - same description
        this.bobPeer.getASAPHubManager().setReconnectIntervalInSeconds(1);

        this.alicePeer.getASAPHubManager().connectASAPHubs();
        this.bobPeer.getASAPHubManager().connectASAPHubs(HubConnectorProtocol.TCP); // variant .. specify connector type.

        Thread.sleep(3000);

        // TODO will not work with three peers... callback connectedAndopen does not work...

        /*
        this.alicePeer.connectASAPHub(hubDescription); // variant

        this.alicePeer.disconnectASAPHubs();
        this.alicePeer.disconnectASAPHub(hubDescription); // variant
        this.bobPeer.disconnectASAPHubs(HubConnectorProtocol.TCP); // variant .. specify connector type.

        this.alicePeer.removeASAPHub(hubDescription);
        this.bobPeer.removeASAPHubs(HubConnectorProtocol.TCP); // variant
        this.bobPeer.removeASAPHubs(); // variant
         */

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
