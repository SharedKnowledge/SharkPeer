package net.sharksystem;

import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPPeer;
import net.sharksystem.hub.ASAPHubException;
import net.sharksystem.hub.HubConnectorDescription;
import net.sharksystem.hub.HubConnectorProtocol;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

/**
 * A Shark (Shared Knowledge) Peer is considered to be host of a collection of decentralized
 * applications based on ASAP. Each Shark peer will host just a single ASAPPeer
 * but usually more than one ASAP application.
 * <br/><br/>
 * Your application can become part of a larger system. Provide a facade of your application
 * that implements SharkComponent. Provide a factory that can setup your application. That's all.
 *
 * @see SharkComponent
 * @see SharkComponentFactory
 * @see ASAPPeer
 */
public interface SharkPeer {
    /**
     * Add a component to the Shark app
     * @param componentFactory
     * @param facade interface
     * @throws SharkException a) wrong status. Peer is already running. b) there is already a
     * component supporting same format.
     */
    void addComponent(SharkComponentFactory componentFactory, Class<? extends SharkComponent> facade)
            throws SharkException;

    /**
     * Remove a component from your Shark application
     * @param facade
     * @throws SharkException a) wrong status. Peer is already running. b) there is no component
     * for this format
     */
    void removeComponent(Class<? extends SharkComponent> facade)
            throws SharkException;

    /**
     *
     * @param facade
     * @return component supporting this format.
     * @throws SharkException No component supports this format
     */
    SharkComponent getComponent(Class<? extends SharkComponent> facade) throws SharkException;

    /**
     * Start the Shark peer. An ASAP peer will be launched with listening to all format from all
     * components. Components can neither be added nor withdrawn after launch.
     * @throws SharkException Exception can only be caused by ASAP peer launch
     */
    void start() throws SharkException;

    /**
     * Start the Shark peer. An ASAP peer will be launched with listening to all format from all
     * components. Components can neither be added nor withdrawn after launch.
     * @param asapPeer use this asap peer instead some default
     * @throws SharkException Exception can only be caused by ASAP peer launch
     */
    void start(ASAPPeer asapPeer) throws SharkException;

    /**
     * Stop that peer. ASAP peer will be stopped.
     * @throws SharkException
     */
    void stop() throws SharkException;

    /**
     *
     * @return current status.
     */
    SharkPeerStatus getStatus();

    /**
     * @return reference to the ASAPPeer object. It is a good idea to use this interface to get that reference
     * when using your ASAP app as Shark component.
     * @throws SharkException e.g. from status - Shark Peer not yet started
     */
    ASAPPeer getASAPPeer() throws SharkException;

    /**
     *
     * @return set of all formats supported by all added components
     */
    Set<CharSequence> getFormats();

    /**
     * Returns true if both peer represent the same peer - IDs are compared.
     * @param otherPeer
     * @throws SharkException peer not yet set and/or initialized
     * @return
     */
    boolean samePeer(SharkPeer otherPeer) throws SharkException;

    boolean samePeer(CharSequence otherPeerID) throws SharkException;

    CharSequence getPeerID() throws SharkException;

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
