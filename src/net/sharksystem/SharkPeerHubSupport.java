package net.sharksystem;

import net.sharksystem.asap.ASAPException;
import net.sharksystem.hub.peerside.HubConnectorDescription;

import java.io.IOException;
import java.util.Collection;

/**
 * Shark peer allows storing settings, like hub management. Those features are described here.
 */
public interface SharkPeerHubSupport {
    CharSequence getPeerID() throws SharkException;

    void addHubDescription(HubConnectorDescription hubConnectorDescription);

    void removeHubDescription(HubConnectorDescription hubConnectorDescription);

    Collection<HubConnectorDescription> getHubDescriptions();

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
}
