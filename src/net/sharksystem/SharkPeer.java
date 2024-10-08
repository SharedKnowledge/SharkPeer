package net.sharksystem;

import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPPeer;
import net.sharksystem.fs.ExtraData;

import java.io.IOException;
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
public interface SharkPeer extends SharkPeerBasic {
    String ANONYMOUS_SHARK_NAME =  "noSharkName";

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
     *
     * @return set of all formats supported by all added components
     */
    Set<CharSequence> getFormats();

    /**
     * Start the Shark peer. An ASAP peer will be launched with listening to all format from all
     * components. Components can neither be added nor withdrawn after launch.
     * @throws SharkException Exception can only be caused by ASAP peer launch
     * @deprecated only for tests acceptable. A blank asap peer will be created; no keystore would be set.
     * No cryptography whatsoever would function.
     */
    void start(CharSequence peerID) throws SharkException;

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

    ExtraData getSharkPeerExtraData() throws SharkException, IOException;

}
