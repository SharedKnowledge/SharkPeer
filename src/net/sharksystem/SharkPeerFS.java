package net.sharksystem;

import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPPeer;
import net.sharksystem.asap.ASAPPeerFS;
import net.sharksystem.asap.EncounterConnectionType;
import net.sharksystem.hub.*;
import net.sharksystem.hub.peerside.HubConnector;
import net.sharksystem.hub.peerside.NewConnectionListener;
import net.sharksystem.hub.peerside.SharedTCPChannelConnectorPeerSide;
import net.sharksystem.utils.Log;

import java.io.IOException;
import java.util.*;

public class SharkPeerFS implements SharkPeer, NewConnectionListener {
    protected final CharSequence owner;
    protected final CharSequence rootFolder;
    private HashMap<CharSequence, SharkComponentFactory> factories = new HashMap<>();
    protected HashMap<CharSequence, SharkComponent> components = new HashMap<>();
    private SharkPeerStatus status = SharkPeerStatus.NOT_INITIALIZED;
    private ASAPPeer asapPeer;

    public SharkPeerFS(CharSequence owner, CharSequence rootFolder) {
        this.owner = owner;
        this.rootFolder = rootFolder;
    }

    private static final String MISSING_FORMAT_EXCEPTION_MESSAGE =
            "annotation missing or parameters parameters: " +
            "@" + ASAPFormats.class.getSimpleName() + "(formats = {\"formatA\"[, \"formatB\"]})";

    private Set<String> getFormats(Class<? extends SharkComponent> facade) throws SharkException {
        try {
            ASAPFormats annotation = facade.getAnnotation(ASAPFormats.class);
            String[] formats = annotation.formats();
            if(formats == null || formats.length < 1) {
                throw new SharkException(MISSING_FORMAT_EXCEPTION_MESSAGE);
            }
            Set<String> formatSet = new HashSet<>();
            for(int i = 0; i < formats.length; i++) {
                formatSet.add(formats[i]);
            }

            return formatSet;
        }
        catch(RuntimeException re) {
            throw new SharkException(MISSING_FORMAT_EXCEPTION_MESSAGE);
        }
    }

    @Override
    public void addComponent(SharkComponentFactory componentFactory,
                             Class<? extends SharkComponent> facade)
            throws SharkException {

        if(this.status != SharkPeerStatus.NOT_INITIALIZED) {
            throw new SharkException("Components cannot be added to a running Shark Peer");
        }

        Set<String> componentFormats = this.getFormats(facade);

        if(componentFormats == null || componentFormats.size() < 1) {
            throw new SharkException("Components must support at least one format. Got null or empty list");
        }

        for(CharSequence format : componentFormats) {
            if(this.components.get(format) != null) {
                throw new SharkException("There is already a component with using format: " + format);
            }

            for(int i = 0; i < SharkComponent.reservedFormats.length; i++) {
                if(format.toString().equalsIgnoreCase(SharkComponent.reservedFormats[i].toString())) {
                    throw new SharkException("You must not use a reserved format: " + format);
                }
            }
        }

        Log.writeLog(this, "create component");
        SharkComponent component = componentFactory.getComponent();

        for(CharSequence format : componentFormats) {
            Log.writeLog(this, "added component that supports format " + format);
            this.components.put(format, component);
        }
    }

    @Override
    public void removeComponent(Class<? extends SharkComponent> facade)
            throws SharkException {

        if(this.status != SharkPeerStatus.NOT_INITIALIZED) {
            throw new SharkException("Components cannot be removed from a running Shark Peer");
        }

        Set<String> componentFormats = this.getFormats(facade);

        for(CharSequence format : componentFormats) {
            this.components.remove(format);
            Log.writeLog(this, "removed component that supported format " + format);
        }
    }

    @Override
    public SharkComponent getComponent(Class<? extends SharkComponent> facade) throws SharkException {
        Set<String> formats = this.getFormats(facade);
        StringBuilder sb = new StringBuilder();
        for(CharSequence format: formats) {
            SharkComponent component = this.components.get(format);
            if(component != null) return component;
            sb.append(format);
            sb.append(" | ");
        }

        throw new SharkException("no component found with format(s) " + sb.toString());
    }

    protected ASAPPeerFS createASAPPeer() throws IOException, ASAPException {
        return new ASAPPeerFS(this.owner, this.rootFolder, this.components.keySet());
    }

    @Override
    public void start() throws SharkException {
        if(this.status != SharkPeerStatus.NOT_INITIALIZED) {
            throw new SharkException("Shark Peer is already running");
        }

        try {
            this.start(this.createASAPPeer());
        } catch (IOException | ASAPException e) {
            e.printStackTrace();
            Log.writeLogErr(this, "could not start ASAP peer - fatal, give up");
            throw new SharkException(e);
        }
    }

    @Override
    public void start(ASAPPeer asapPeer) throws SharkException {
        this.asapPeer = asapPeer;
        boolean fullSuccess = true; // optimistic
        for(SharkComponent component : this.components.values()) {
            try {
                component.onStart(this.asapPeer);
            } catch (SharkException e) {
                Log.writeLogErr(this, "could not start component: " + e.getLocalizedMessage());
                throw e;
            }
        }

        this.status = SharkPeerStatus.RUNNING;
        if(fullSuccess) {
            Log.writeLog(this, "Shark system started");
        } else {
            Log.writeLog(this, "Shark system started with errors.");
        }
    }

    @Override
    public void stop() throws SharkException {
        if(this.status != SharkPeerStatus.RUNNING) {
            throw new SharkException("Shark Peer is not running");
        }
        Log.writeLog(this, "Shark system stopped");
    }

    @Override
    public SharkPeerStatus getStatus() {
        return this.status;
    }

    public boolean samePeer(SharkPeer otherPeer) throws SharkException {
        return this.getASAPPeer().samePeer(otherPeer.getASAPPeer());
    }

    public boolean samePeer(CharSequence otherPeerID) throws SharkException {
        return this.getASAPPeer().samePeer(otherPeerID);
    }

    @Override
    public ASAPPeer getASAPPeer() throws SharkException {
        if(this.status != SharkPeerStatus.RUNNING) {
            throw new SharkException("Shark Peer is not running");
        }

        if(this.asapPeer == null) {
            throw new SharkException("That's a bug: ASAP peer not created");
        }

        return this.asapPeer;
    }

    @Override
    public Set<CharSequence> getFormats() {
        return this.components.keySet();
    }

    @Override
    public CharSequence getPeerID() throws SharkException {
        return this.getASAPPeer().getPeerID();
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                              hub management                                                //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private List<HubConnectorDescription> hubList = new ArrayList<>();
    public static final String HUB_LIST_KEY = "SharkPeerHubList";

    //// persist hub list
    private void saveHubList() throws IOException {
        try {
            this.putExtra(HUB_LIST_KEY, HubConnectorAlgebra.serializeCollection(this.hubList));
        } catch (SharkException | ASAPException e) {
            // not yet launched.. hopefully it does not get lost in memory before launch
        }
    }

    private void restoreHubList() {
        try {
            try {
                byte[] serializedHubList = this.getExtra(HUB_LIST_KEY);
                if(serializedHubList == null) {
                    this.hubList = new ArrayList<>();
                    return;
                }
                this.hubList = HubConnectorAlgebra.deserializeList(serializedHubList);
            } catch (SharkException e) {
                // system not yet launched
                return;
            }
        } catch (ASAPException | IOException e) {
            Log.writeLogErr(this, "cannot restore extra data");
        }
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

    @Override
    public void connectASAPHubs() throws ASAPHubException, SharkException, IOException {
        for (HubConnectorDescription inList : this.hubList) {
            this.connectASAPHub(inList);
        }
    }

    @Override
    public void connectASAPHubs(HubConnectorProtocol connectionType) throws ASAPHubException, SharkException, IOException {
        for (HubConnectorDescription inList : this.hubList) {
            if (inList.getHubConnectorType() == connectionType) {
                this.connectASAPHub(inList);
            }
        }
    }

    private Map<HubConnectorDescription, HubConnector> activeHubConnections = new HashMap<>();

    @Override
    public void connectASAPHub(HubConnectorDescription hubDescription)
            throws ASAPHubException, IOException, SharkException {

        switch(hubDescription.getHubConnectorType()) {
            case TCP:
                TCPHubConnectorDescription tcpDescription = (TCPHubConnectorDescription) hubDescription;
                HubConnector tcpHubConnector = SharedTCPChannelConnectorPeerSide.createTCPHubConnector(
                        tcpDescription.getHubHostName(), tcpDescription.getHubHostPort());
                tcpHubConnector.setListener(this);
                tcpHubConnector.connectHub(this.getPeerID());
                this.activeHubConnections.put(hubDescription, tcpHubConnector);
                break;
            default: throw new SharkNotSupportedException("unsupported connection type / protocol");
        }

    }

    @Override
    public void notifyPeerConnected(StreamPair streamPair) {
        try {
            Log.writeLog(this, "new connection via hub... [TODO] security settings (encrypt / signing)");
            this.asapPeer.handleConnection(
                    streamPair.getInputStream(),
                    streamPair.getOutputStream(), false, false, EncounterConnectionType.ASAP_HUB);
        } catch (Exception e) {
            Log.writeLogErr(this, e.getLocalizedMessage());
        }
    }

    @Override
    public void disconnectASAPHubs() throws ASAPHubException, IOException {
        this.disconnectASAPHubs(this.activeHubConnections.values());
    }

    @Override
    public void disconnectASAPHubs(HubConnectorProtocol connectionType) throws ASAPHubException, IOException {
        List<HubConnector> activeConnections = new ArrayList<>();

        for(HubConnectorDescription description : this.activeHubConnections.keySet()) {
            if(description.getHubConnectorType() == connectionType) {
                activeConnections.add(this.activeHubConnections.get(description));
            }
        }
        this.disconnectASAPHubs(activeConnections);
    }

    @Override
    public void disconnectASAPHub(HubConnectorDescription hubDescription) throws ASAPHubException, IOException {
        // find matching active connection

        for(HubConnectorDescription description : this.activeHubConnections.keySet()) {
            if(HubConnectorAlgebra.same(hubDescription, description)) {
                List<HubConnector> connectionList = new ArrayList<>();
                connectionList.add(this.activeHubConnections.get(description));
                this.disconnectASAPHubs(connectionList);
                return; // just one.
            }
        }
    }

    private void disconnectASAPHubs(Collection<HubConnector> hubConnectorList) throws ASAPHubException, IOException {
        for(HubConnector connector : hubConnectorList) {
            connector.disconnectHub();
        }
    }
}
