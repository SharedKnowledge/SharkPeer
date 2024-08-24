package net.sharksystem;

/**
 * Factory that produces an instance of your ASAP app which can be used as Shark application component.
 *
 * Please read the following section carefully.
 *
 * Most components would require ids or names. And that's sometimes somewhat tricky.
 *
 * <ul>
 * <li>An ID should be considered unique and stable. A name can change.</li>
 * <li>An ASAP peer does not have a name but an id.</li>
 * <li>A Shark peer can be named when it is created. That's not an id, though.</li>
 * <li>A Shark Peer is started with an ASAP. Now, it has got access to a peer.</li>
 * </ul>
 *
 * What does this have to do with a Shark Component? Well, in most cases, a component needs an id. Sometimes it
 * needs a more readable name. A name is provided by a shark peer at any time. An id can only be provided, when an
 * ASAP peer was created and attached to a SharkPeer.
 *
 * A factory object is created before an ASAP peer exists. The factory method is called after an ASAP peer has
 * started and attached to a SharkPeer. If you need a name for your component. You can get it anytime from a
 * a SharkPeer. If you need an id - get it when your component object is created with getComponent. You can be sure that
 * sharkPeer.getPeerID() will work at that moment.
 */
public interface SharkComponentFactory {
    /**
     * Get a component object (most probably a singleton)
     * @param sharkPeer
     * @return
     * @throws SharkException if something went wrong - problem is documented in this exception
     */
    SharkComponent getComponent(SharkPeer sharkPeer) throws SharkException;
}
