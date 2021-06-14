package net.sharksystem.hub;

import net.sharksystem.SharkException;
import net.sharksystem.SharkNotSupportedException;
import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPPeer;
import net.sharksystem.asap.EncounterConnectionType;
import net.sharksystem.hub.peerside.HubConnector;
import net.sharksystem.hub.peerside.NewConnectionListener;
import net.sharksystem.hub.peerside.SharedTCPChannelConnectorPeerSide;
import net.sharksystem.utils.Log;

import java.io.IOException;
import java.util.*;

public abstract class ASAPHubManagerImpl implements ASAPHubManager, NewConnectionListener {
    private final ASAPPeer asapPeer;

    public ASAPHubManagerImpl(ASAPPeer asapPeer) {
        this.asapPeer = asapPeer;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                              hub management                                                //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private List<HubConnectorDescription> hubList = new ArrayList<>();
    public static final String HUB_LIST_KEY = "SharkPeerHubList";

    //// persist hub list
    private void saveHubList() throws IOException {
        try {
            this.asapPeer.putExtra(HUB_LIST_KEY, HubConnectorAlgebra.serializeCollection(this.hubList));
        } catch (ASAPException e) {
            // not yet launched.. hopefully it does not get lost in memory before launch
        }
    }

    private void restoreHubList() {
        try {
            byte[] serializedHubList = this.asapPeer.getExtra(HUB_LIST_KEY);
            if(serializedHubList == null) {
                this.hubList = new ArrayList<>();
                return;
            }
            this.hubList = HubConnectorAlgebra.deserializeList(serializedHubList);
        } catch (ASAPException | IOException e) {
            Log.writeLogErr(this, "cannot restore extra data");
        }
    }


    protected List<HubConnectorDescription> getHubList() {
        this.restoreHubList();
        return this.hubList;
    }

    @Override
    public void addASAPHub(HubConnectorDescription hubDescription) throws IOException {
        // already in there?
        for(HubConnectorDescription inList : this.hubList) {
            if(HubConnectorAlgebra.same(inList, hubDescription)) {
                // already in there
                return;
            }
        }

        this.hubList.add(hubDescription);
        this.saveHubList();
    }

    public Collection<HubConnectorDescription> getHubs() {
        this.restoreHubList();
        return this.hubList;
    }

    public Collection<HubConnectorDescription> getHubs(HubConnectorProtocol connectionType) {
        this.restoreHubList();
        List<HubConnectorDescription> retList = new ArrayList<>();
        for (HubConnectorDescription inList : this.hubList) {
            if (inList.getHubConnectorType() == connectionType) {
                retList.add(inList);
            }
        }

        return retList;
    }

    @Override
    public void removeASAPHub(HubConnectorDescription hubDescription) throws IOException {
        if (!this.hubList.remove(hubDescription)) {
            // not that object found - but maybe some that describes the same hub?
            HubConnectorDescription toRemove = null;
            for (HubConnectorDescription inList : this.hubList) {
                if (HubConnectorAlgebra.same(inList, hubDescription)) {
                    // already in there
                    toRemove = inList; // remove while iterating produced problems sometimes
                    break;
                }
            }
            if(toRemove != null) this.hubList.remove(toRemove);
        }

        this.saveHubList();
    }

    @Override
    public void removeASAPHubs(HubConnectorProtocol connectionType) throws IOException {
        List<HubConnectorDescription> toRemoveList = new ArrayList<>();
        for (HubConnectorDescription inList : this.hubList) {
            if (inList.getHubConnectorType() == connectionType) {
                toRemoveList.add(inList);
            }
        }

        for (HubConnectorDescription toRemove : toRemoveList) {
            this.hubList.remove(toRemove);
        }

        this.saveHubList();
    }

    @Override
    public void removeASAPHubs() throws IOException {
        this.hubList = new ArrayList<>();
        this.saveHubList();
    }

    public ASAPPeer getASAPPeer() {
        return this.asapPeer;
    }
}

