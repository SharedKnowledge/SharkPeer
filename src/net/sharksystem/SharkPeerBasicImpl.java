package net.sharksystem;

import net.sharksystem.asap.*;
import net.sharksystem.asap.utils.ASAPSerialization;
import net.sharksystem.hub.peerside.HubConnectorDescription;
import net.sharksystem.hub.peerside.HubConnectorFactory;
import net.sharksystem.utils.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class SharkPeerBasicImpl implements SharkPeerBasic, ASAPEnvironmentChangesListener {
    private ASAPPeer asapPeer;
    private CharSequence sharkName;

    public SharkPeerBasicImpl() { }

    private void init() {
        this.restoreHubDescriptions();
    }

    /**
     * Create the Shark peer as a shell - no asap peer inside yet
     * @param sharkName
     */
    public SharkPeerBasicImpl(CharSequence sharkName) {
        this.sharkName = sharkName;
    }

    /**
     * Create a Shark peer with a name and an asap peer inside. System is apparently running.
     * @param sharkName
     * @param asapPeer
     */
    public SharkPeerBasicImpl(CharSequence sharkName, ASAPPeer asapPeer) {
        this.sharkName = sharkName;
        this.asapPeer = asapPeer;
        this.init();
    }

    /**
     * Create a Shark peer (without a specific name) with its ASAP peer.
     * @param asapPeer
     */
    public SharkPeerBasicImpl(ASAPPeer asapPeer) {
        this(SharkPeer.ANONYMOUS_SHARK_NAME, asapPeer);
    }

    protected void setASAPPeer(ASAPPeer asapPeer) {
        this.asapPeer = asapPeer;
        this.init();
    }

    public ASAPPeer getASAPPeer() throws SharkException {
        if(this.asapPeer == null) throw new SharkException("asap peer not set (yet)");
        return this.asapPeer;
    }

    @Override
    public CharSequence getPeerID() throws SharkException {
        return this.getASAPPeer().getPeerID();
    }

    @Override
    public CharSequence getSharkPeerName() {
        return this.sharkName;
    }


    public boolean samePeer(SharkPeer otherPeer) throws SharkException {
        return this.getASAPPeer().samePeer(otherPeer.getASAPPeer());
    }

    public boolean samePeer(CharSequence otherPeerID) throws SharkException {
        return this.getASAPPeer().samePeer(otherPeerID);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                      hub description management                                       //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static final CharSequence HUB_DESCRIPTIONS = "hubDescriptions";
    private List<HubConnectorDescription> hubConnectorDescriptions = new ArrayList<>();

    private void checkHubDescriptionsRestored() {
        if(!this.hubDescriptionsRestored) {
            this.restoreHubDescriptions();
        }
    }

    @Override
    public void addHubDescription(HubConnectorDescription hubConnectorDescription) {
        this.checkHubDescriptionsRestored();
        if(hubConnectorDescription == null) return;

        // duplicate suppression
        for(HubConnectorDescription hcd : this.hubConnectorDescriptions) {
            if(hcd.isSame(hubConnectorDescription)) return;
        }

        this.hubConnectorDescriptions.add(hubConnectorDescription);
        this.persistHubDescriptions();
    }

    @Override
    public void removeHubDescription(HubConnectorDescription hubConnectorDescription) {
        this.checkHubDescriptionsRestored();
        HubConnectorDescription same = null;
        for(HubConnectorDescription hcd : this.hubConnectorDescriptions) {
            if(hubConnectorDescription.isSame(hcd)) {
                same = hcd;
                break;
            }
        }

        if(same != null) this.hubConnectorDescriptions.remove(same);
        this.persistHubDescriptions();
    }

    @Override
    public List<HubConnectorDescription> getHubDescriptions() {
        this.checkHubDescriptionsRestored();
        return this.hubConnectorDescriptions;
    }

    @Override
    public HubConnectorDescription getHubDescription(int index) throws SharkException {
        this.checkHubDescriptionsRestored();
        if(this.hubConnectorDescriptions.size() <= index) throw new SharkException("index out of range");

        return this.hubConnectorDescriptions.get(index);
    }

    private void persistHubDescriptions() {
        // not yet started or nothing to do
        if(this.hubConnectorDescriptions.isEmpty() || this.asapPeer == null) return;

        byte[][] serializedDescriptions = new byte[this.hubConnectorDescriptions.size()][];
        int index = 0;
        try {
            for(HubConnectorDescription hcd : this.hubConnectorDescriptions) {
                serializedDescriptions[index++] = hcd.serialize();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ASAPSerialization.writeByteArray(serializedDescriptions, baos);
            byte[] serial = baos.toByteArray();

            this.asapPeer.putExtra(HUB_DESCRIPTIONS, serial);
        } catch (IOException | ASAPException e) {
            Log.writeLogErr(this, "cannot serialized hub description");
            return;
        }

    }

    private boolean hubDescriptionsRestored = false;
    private void restoreHubDescriptions() {
        if(this.asapPeer == null) return; // not yet started
        if(this.hubDescriptionsRestored) return; // only once

        this.hubDescriptionsRestored = true;

        byte[] serial = null;
        try {
            serial = this.asapPeer.getExtra(HUB_DESCRIPTIONS);
            if(serial == null) return; // ok - no descriptions stored
        } catch (ASAPException | IOException e) {
            Log.writeLog(this, "cannot read hub description - ok, maybe there are non");
            return;
        }

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(serial);
            byte[][] serializedDescriptions = ASAPSerialization.readByte2DimArray(bais);

            for(int i = 0; i < serializedDescriptions.length; i++) {
                this.hubConnectorDescriptions.add(
                        HubConnectorFactory.createHubConnectorByDescription(serializedDescriptions[i]));
            }
        } catch (IOException | ASAPException e) {
            Log.writeLogErr(this, "cannot deserialize hub description - seems to be a bug");
        }
    }

    private Set<SharkPeerEncounterChangedListener> sharkPeerEncounterChangedListenerSet = new HashSet<>();
    boolean listenEnvironmentChange = false;
    @Override
    public void addSharkPeerEncounterChangedListener(
            SharkPeerEncounterChangedListener sharkPeerEncounterChangedListener) throws SharkException {
        this.sharkPeerEncounterChangedListenerSet.add(sharkPeerEncounterChangedListener);
        if(!this.listenEnvironmentChange) {
            this.getASAPPeer().addASAPEnvironmentChangesListener(this);
        }
    }

    public void removeSharkPeerEncounterChangedListener(
            SharkPeerEncounterChangedListener sharkPeerEncounterChangedListener) {
        this.sharkPeerEncounterChangedListenerSet.remove(sharkPeerEncounterChangedListener);

        if(this.sharkPeerEncounterChangedListenerSet.isEmpty()) {
            this.previousEncounterList = null;
            this.asapPeer.removeASAPEnvironmentChangesListener(this);
        }
    }

    // listen to environment changes in ASAP peer
    private Set<CharSequence> previousEncounterList = null;
    @Override
    public void onlinePeersChanged(Set<CharSequence> newEncounterList) {
        if(this.sharkPeerEncounterChangedListenerSet.isEmpty()) {
            this.previousEncounterList = null;
            return;
        }

        if(this.previousEncounterList == null) {
            this.previousEncounterList = newEncounterList; // got first encounter list
            this.notifyAboutEncounter(newEncounterList, true);
            return;
        }

        // check what changed
        Set<CharSequence> lostIDs;
        Set<CharSequence> addedIDs = new HashSet<>();
        Log.writeLog(this, "BREAK 1", "ConcurrentModification");

        for(CharSequence idFromNewList : newEncounterList) {
            if(!this.previousEncounterList.contains(idFromNewList)) { // it is new
                addedIDs.add(idFromNewList);
            } else {
                // was already in there - remove it
                this.previousEncounterList.remove(idFromNewList);
            }
        }
        this.notifyAboutEncounter(addedIDs, true);
        this.notifyAboutEncounter(this.previousEncounterList, false);
    }

    private void notifyAboutEncounter(Set<CharSequence> changedIDs, boolean added) {
        for(CharSequence changedID : changedIDs) {
            for (SharkPeerEncounterChangedListener listener : this.sharkPeerEncounterChangedListenerSet) {
                (new Thread() {
                    public void run() {
                        if(added) listener.encounterStarted(changedID);
                        else listener.encounterTerminated(changedID);
                    }
                }).start();
            }
        }
    }
}
