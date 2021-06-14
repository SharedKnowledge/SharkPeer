package net.sharksystem.hub;

import net.sharksystem.asap.ASAPPeer;
import net.sharksystem.hub.peerside.HubConnector;
import net.sharksystem.hub.peerside.NewConnectionListener;

import java.io.IOException;

public class HubConnectionManager implements NewConnectionListener {
    public HubConnectionManager(ASAPPeer asapPeer, HubConnector connector) throws ASAPHubException, IOException {
        connector.setListener(this);
        connector.connectHub(asapPeer.getPeerID());
    }

    @Override
    public void notifyPeerConnected(StreamPair streamPair) {

    }

    public void disconnectHub() {

    }
}
