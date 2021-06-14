package net.sharksystem.hub;

import net.sharksystem.SharkException;
import net.sharksystem.SharkNotSupportedException;
import net.sharksystem.asap.ASAPPeer;
import net.sharksystem.asap.EncounterConnectionType;
import net.sharksystem.hub.peerside.HubConnector;
import net.sharksystem.hub.peerside.SharedTCPChannelConnectorPeerSide;
import net.sharksystem.utils.Log;

import java.io.IOException;
import java.util.*;

public class ASAPHubManagerServiceSide extends ASAPHubManagerImpl {
    public ASAPHubManagerServiceSide(ASAPPeer asapPeer) {
        super(asapPeer);
    }

    private Map<HubConnectorDescription, HubConnector> activeHubConnections = new HashMap<>();

    @Override
    public void connectASAPHub(HubConnectorDescription hubDescription)
            throws ASAPHubException, IOException, SharkException {

        switch(hubDescription.getHubConnectorType()) {
            case TCP:
                TCPHubConnectorDescription tcpDescription = (TCPHubConnectorDescription) hubDescription;
                HubConnector tcpHubConnector = SharedTCPChannelConnectorPeerSide.createTCPHubConnector(
                        tcpDescription.getHubHostName(), tcpDescription.getHubHostPort());
                tcpHubConnector.setListener(this);
                tcpHubConnector.connectHub(this.getASAPPeer().getPeerID());
                this.activeHubConnections.put(hubDescription, tcpHubConnector);
                break;
            default: throw new SharkNotSupportedException("unsupported connection type / protocol");
        }
    }

    @Override
    public void connectASAPHubs() throws ASAPHubException, SharkException, IOException {
        for (HubConnectorDescription inList : this.getHubList()) {
            this.connectASAPHub(inList);
        }
    }

    @Override
    public void connectASAPHubs(HubConnectorProtocol connectionType) throws ASAPHubException, SharkException, IOException {
        for (HubConnectorDescription inList : this.getHubList()) {
            if (inList.getHubConnectorType() == connectionType) {
                this.connectASAPHub(inList);
            }
        }
    }

    @Override
    public void notifyPeerConnected(StreamPair streamPair) {
        try {
            Log.writeLog(this, "new connection via hub... [TODO] security settings (encrypt / signing)");
            this.getASAPPeer().handleConnection(
                    streamPair.getInputStream(),
                    streamPair.getOutputStream(), false, false,
                    EncounterConnectionType.ASAP_HUB);
        } catch (Exception e) {
            Log.writeLogErr(this, e.getLocalizedMessage());
        }
    }

    @Override
    public void disconnectASAPHubs() throws ASAPHubException, IOException {
        this.disconnectASAPHubs(this.activeHubConnections.values());
    }

    @Override
    public void disconnectASAPHubs(HubConnectorProtocol connectionType) throws ASAPHubException, IOException {
        List<HubConnector> activeConnections = new ArrayList<>();

        for(HubConnectorDescription description : this.activeHubConnections.keySet()) {
            if(description.getHubConnectorType() == connectionType) {
                activeConnections.add(this.activeHubConnections.get(description));
            }
        }
        this.disconnectASAPHubs(activeConnections);
    }

    @Override
    public void disconnectASAPHub(HubConnectorDescription hubDescription) throws ASAPHubException, IOException {
        // find matching active connection

        for(HubConnectorDescription description : this.activeHubConnections.keySet()) {
            if(HubConnectorAlgebra.same(hubDescription, description)) {
                List<HubConnector> connectionList = new ArrayList<>();
                connectionList.add(this.activeHubConnections.get(description));
                this.disconnectASAPHubs(connectionList);
                return; // just one.
            }
        }
    }

    private void disconnectASAPHubs(Collection<HubConnector> hubConnectorList) throws ASAPHubException, IOException {
        for(HubConnector connector : hubConnectorList) {
            connector.disconnectHub();
        }
    }
}
