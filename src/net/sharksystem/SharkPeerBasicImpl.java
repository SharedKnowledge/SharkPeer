package net.sharksystem;

import net.sharksystem.asap.ASAPEncounterManager;
import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPPeer;
import net.sharksystem.asap.EncounterManagerAdmin;
import net.sharksystem.asap.utils.ASAPSerialization;
import net.sharksystem.hub.peerside.HubConnectorDescription;
import net.sharksystem.hub.peerside.HubConnectorFactory;
import net.sharksystem.utils.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SharkPeerBasicImpl implements SharkPeerBasic {
    protected EncounterManagerAdmin encounterManagerAdmin;
    private ASAPPeer asapPeer;

    public SharkPeerBasicImpl() { }

    private void init() {
        this.restoreHubDescriptions();
    }

    /**
     * @deprecated An ASAPPeer should be presented when we start() a SharkPeer and
     * <b>not during</b> its construction
     * @param asapPeer
     */
    public SharkPeerBasicImpl(ASAPPeer asapPeer) {
        this.asapPeer = asapPeer;
        this.init();
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                              extra data                                                    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void putExtra(CharSequence key, byte[] value) throws IOException, SharkException, ASAPException {
        if(this.asapPeer == null) {
            throw new SharkException("peer is not yet launched - initialize your shark system");
        }
        this.asapPeer.putExtra(key, value);
    }

    @Override
    public byte[] getExtra(CharSequence key) throws ASAPException, IOException, SharkException {
        if(this.asapPeer == null) {
            throw new SharkException("peer is not yet launched - initialize your shark system");
        }
        return this.asapPeer.getExtra(key);
    }

}
