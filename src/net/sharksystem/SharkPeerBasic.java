package net.sharksystem;

import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPPeer;
import net.sharksystem.hub.peerside.HubConnectorDescription;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Shark peer allows storing settings, like hub management. Those features are described here.
 */
public interface SharkPeerBasic {
    CharSequence getPeerID() throws SharkException;
    CharSequence getSharkPeerName();

    void addHubDescription(HubConnectorDescription hubConnectorDescription);

    void removeHubDescription(HubConnectorDescription hubConnectorDescription);

    List<HubConnectorDescription> getHubDescriptions();

    HubConnectorDescription getHubDescription(int index) throws SharkException;

    /**
     * Make a value persistent with key
     * @param key
     * @param value
     */
    void putExtra(CharSequence key, byte[] value) throws IOException, SharkException, ASAPException;

    /**
     * Return a value. Throws an exception if not set
     * @param key
     * @throws ASAPException key never used in putExtra
     */
    byte[] getExtra(CharSequence key) throws ASAPException, IOException, SharkException;

    void addSharkPeerEncounterChangedListener(SharkPeerEncounterChangedListener sharkPeerEncounterChangedListener) throws SharkException;


    /**
     * @return reference to the ASAPPeer object. It is a good idea to use this interface to get that reference
     * when using your ASAP app as Shark component.
     * @throws SharkException e.g. from status - Shark Peer not yet started
     */
    ASAPPeer getASAPPeer() throws SharkException;

    /**
     * Returns true if both peer represent the same peer - IDs are compared.
     * @param otherPeer
     * @throws SharkException peer not yet set and/or initialized
     * @return
     */
    boolean samePeer(SharkPeer otherPeer) throws SharkException;

    boolean samePeer(CharSequence otherPeerID) throws SharkException;
}
