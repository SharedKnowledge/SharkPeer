package net.sharksystem.serviceSide;

import net.sharksystem.SharkConnectionManager;
import net.sharksystem.SharkException;
import net.sharksystem.asap.ASAPConnectionHandler;
import net.sharksystem.asap.ASAPEncounterManagerImpl;
import net.sharksystem.asap.ASAPPeer;
import net.sharksystem.hub.peerside.ASAPHubManagerImpl;
import net.sharksystem.hub.peerside.HubConnectorDescription;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SharkConnectionManagerServiceSide implements SharkConnectionManager {
    private final ASAPConnectionHandler asapConnectionHandler;
    private final ASAPPeer asapPeer;
    private ASAPEncounterManagerImpl encounterManager;
    private ASAPHubManagerImpl hubManager;

    SharkConnectionManagerServiceSide(ASAPConnectionHandler asapConnectionHandler, ASAPPeer asapPeer) {
        this.asapConnectionHandler = asapConnectionHandler;
        this.asapPeer = asapPeer;
        this.encounterManager = new ASAPEncounterManagerImpl(asapConnectionHandler);
        this.hubManager = new ASAPHubManagerImpl(this.encounterManager);
    }

    @Override
    public void connectHub(HubConnectorDescription hcd) throws SharkException, IOException {
        List<HubConnectorDescription> hcdList = this.hubManager.getRunningConnectorDescriptions();
        hcdList.add(hcd);
        this.hubManager.connectASAPHubs(hcdList, this.asapPeer, true);
    }

    @Override
    public void disconnectHub(HubConnectorDescription hcd) throws SharkException, IOException {
        List<HubConnectorDescription> hcdList = this.hubManager.getRunningConnectorDescriptions();
        for(HubConnectorDescription hcdRunning : hcdList) {
            if(hcdRunning.isSame(hcd)) {
                // running
                hcdList.remove(hcdRunning);
                this.hubManager.connectASAPHubs(hcdList, this.asapPeer, true);
            }
        }
    }

    @Override
    public void disconnectHub(int index) throws SharkException, IOException {
        List<HubConnectorDescription> hcdList = this.hubManager.getRunningConnectorDescriptions();
        this.disconnectHub(hcdList.get(index));
    }

    @Override
    public List<HubConnectorDescription> getConnectedHubs() {
        return this.hubManager.getRunningConnectorDescriptions();
    }
}
