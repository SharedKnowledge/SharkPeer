package net.sharksystem.hub;

import net.sharksystem.SharkException;

import java.io.IOException;
import java.util.Collection;

public interface ASAPHubManager {
    /**
     * Add a hub description.
     * @param hubDescription
     */
    void addASAPHub(HubConnectorDescription hubDescription) throws IOException;

    Collection<HubConnectorDescription> getHubs();

    Collection<HubConnectorDescription> getHubs(HubConnectorProtocol connectionType);

    /**
     * Try to connection to all hubs
     */
    void connectASAPHubs() throws ASAPHubException, SharkException, IOException;

    /**
     * Try to connect to all hubs which can be accessed with a defined connection type
     * @param connectionType
     */
    void connectASAPHubs(HubConnectorProtocol connectionType) throws ASAPHubException, SharkException, IOException;

    /**
     * Try to connect to that specific hub
     * @param hubDescription
     */
    void connectASAPHub(HubConnectorDescription hubDescription) throws ASAPHubException, IOException, SharkException;

    /**
     * Disconnect from all hubs
     */
    void disconnectASAPHubs() throws ASAPHubException, IOException;

    /**
     * Disconnect to all hubs which can be accessed with a defined connection type
     * @param connectionType
     */
    void disconnectASAPHubs(HubConnectorProtocol connectionType) throws ASAPHubException, IOException;

    /**
     * Disconnect from specific hub
     * @param hubDescription
     */
    void disconnectASAPHub(HubConnectorDescription hubDescription) throws ASAPHubException, IOException;

    /**
     * Remove a hub description
     * @param hubDescription
     */
    void removeASAPHub(HubConnectorDescription hubDescription) throws IOException;

    /**
     * Remove all hub description of a given connection type
     * @param connectionType
     */
    void removeASAPHubs(HubConnectorProtocol connectionType) throws IOException;

    /**
     * Remove all hub descriptions.
     */
    void removeASAPHubs() throws IOException;

    int DEFAULT_RECONNECT_INTERVAL_IN_SECONDS = 600; // 10 minutes

    /**
     * Connecting to a hub includes two things: Establishing a point-to-point connection to the hub and
     * register this a peer. Afterwards, hub is asked for a list of registered peers. A connection is established to
     * each peer on the list. A break is made after we are trough with the list. This method allows setting break
     * duration in seconds. Default is 600 == 10 minutes.
     *
     * @param seconds
     */
    void setReconnectIntervalInSeconds(int seconds);
}
