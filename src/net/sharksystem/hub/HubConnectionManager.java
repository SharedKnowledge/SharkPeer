package net.sharksystem.hub;

import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPPeer;
import net.sharksystem.asap.EncounterConnectionType;
import net.sharksystem.asap.utils.Helper;
import net.sharksystem.asap.utils.PeerIDHelper;
import net.sharksystem.hub.peerside.HubConnector;
import net.sharksystem.hub.peerside.HubConnectorStatusListener;
import net.sharksystem.hub.peerside.NewConnectionListener;
import net.sharksystem.utils.AlarmClock;
import net.sharksystem.utils.AlarmClockListener;
import net.sharksystem.utils.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class HubConnectionManager implements NewConnectionListener, Runnable, HubConnectorStatusListener,
        AlarmClockListener {
    private int reconnectIntervalInSeconds;
    private final ASAPPeer asapPeer;
    private final HubConnector connector;

    HubConnectionManager(ASAPPeer asapPeer, HubConnector connector, int reconnectIntervalInSeconds)
            throws ASAPHubException, IOException {
        connector.setListener(this);
        connector.addStatusListener(this);
        connector.connectHub(asapPeer.getPeerID());

        this.asapPeer = asapPeer;
        this.connector = connector;
        this.reconnectIntervalInSeconds = reconnectIntervalInSeconds;

        this.contactAllPeers();
    }

    void setReconnectIntervalInSeconds(int seconds) {
        this.reconnectIntervalInSeconds = seconds;
    }

    @Override
    public void notifyPeerConnected(StreamPair streamPair) {
        try {
            this.asapPeer.handleConnection(
                    streamPair.getInputStream(),
                    streamPair.getOutputStream(),
                    false, false,
                    EncounterConnectionType.ASAP_HUB);
        } catch (IOException | ASAPException e) {
            Log.writeLogErr(this, "cannot handle new connection initiated by a hub");
        }
    }


    @Override
    public void notifySynced() {
        if(this.waitingForSynced != null) {
            this.waitingForSynced.interrupt();
            this.waitingForSynced = null;
        }
    }

    private Thread waitingForSynced = null;
    private void wait4Synced() {
        Log.writeLog(this, this.toString(), "wait for sync reply from hub");
        try {
            this.waitingForSynced = Thread.currentThread();
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            // woke up - try again
            Log.writeLog(this, this.toString(), "wait for sync - woke up");
        }
    }


    void disconnectHub() throws ASAPHubException, IOException {
        this.connector.disconnectHub();
    }

    AlarmClock reconnectAlarm = null;
    private void contactAllPeers() {
        if(this.reconnectAlarm != null) {
            this.reconnectAlarm.kill();
        }
        new Thread(this).start(); // launch re-connect round
    }

    @Override
    public void alarmClockRinging(int alarmCode) {
        Log.writeLog(this, this.toString(), "reconnect with peers");
        this.contactAllPeers();
    }

    private boolean connectedAndOpen = false;
    @Override
    public void notifyConnectedAndOpen() {
        Log.writeLog(this, this.toString(), "notified: connected to hub and open streams");
        this.connectedAndOpen = true;
        if(this.waitingThreadForConnectedAndOpen != null) {
            this.waitingThreadForConnectedAndOpen.interrupt();
            this.waitingThreadForConnectedAndOpen = null;
        }
    }

    private Thread waitingThreadForConnectedAndOpen = null;
    private void wait4ConnectedAndOpen() {
        if (!this.connectedAndOpen) {
            Log.writeLog(this, this.toString(), "wait for established and open connection to hub");
            try {
                this.waitingThreadForConnectedAndOpen = Thread.currentThread();
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                // woke up - try again
                Log.writeLog(this, this.toString(), "connected and open - woke up");
            }
        }
    }

    private void connectPeers(Collection<CharSequence> peerIDs) throws IOException {
        for (CharSequence peerID : peerIDs) {
            this.wait4ConnectedAndOpen(); // wait
            this.connectedAndOpen = false; // take it
            Log.writeLog(this, this.toString(), "connection open and ready: try to connect: " + peerID);
            this.connector.connectPeer(peerID);
        }
    }

    private List<CharSequence> connectedPeersLastLoop = null;
    @Override
    public void run() {
        Log.writeLog(this, this.toString(), "connection manager thread started");
        boolean again = true;
        try {
            while(again) {
                again = false; // assume a single loop

                this.wait4ConnectedAndOpen();
                this.connector.syncHubInformation();
                this.wait4Synced();

                Log.writeLog(this, this.toString(), "synced with hub");
                Collection<CharSequence> peerIDs = this.connector.getPeerIDs();

                Log.writeLog(this, this.toString(), "peers@hub: " +
                        peerIDs);
//                Helper.collOfCharSequence2DebugOutput(peerIDs)); // remove from ASAPJava TODO

                List<CharSequence> metPeers = new ArrayList<>();
                List<CharSequence> newPeers = new ArrayList<>();

                // split and make a copy
                if(this.connectedPeersLastLoop != null && !this.connectedPeersLastLoop.isEmpty()) {
                    for(CharSequence peerID : peerIDs) {
                        boolean met = false;
                        for(CharSequence metPeerID : this.connectedPeersLastLoop) {
                            if(PeerIDHelper.sameID(metPeerID, peerID)) {
                                met = true; break;
                            }
                        }
                        if(met) metPeers.add(peerID);
                        else newPeers.add(peerID);
                    }
                } else {
                    for(CharSequence newPeerID : peerIDs) {
                        newPeers.add(newPeerID);
                    }
                }
                Log.writeLog(this, this.toString(), "new peers on hub: " + newPeers);
                Log.writeLog(this, this.toString(), "peers already met on hub " + metPeers);

                long loopStarted = System.currentTimeMillis(); // start clock

                // iterate new Peers first
                this.connectPeers(newPeers);
                this.connectPeers(metPeers);

                long loopEnded = System.currentTimeMillis(); // stop clock

                // merge both list if necessary
                if(newPeers.isEmpty() || metPeers.isEmpty()) {
                    this.connectedPeersLastLoop = metPeers.isEmpty() ? newPeers : metPeers;
                } else {
                    // merge
                    this.connectedPeersLastLoop = new ArrayList<>();
                    for(CharSequence peerID : newPeers) this.connectedPeersLastLoop.add(peerID);
                    for(CharSequence peerID : metPeers) this.connectedPeersLastLoop.add(peerID);
                }

                long duration = loopEnded - loopStarted; // calculate when to start next loop
                Log.writeLog(this, this.toString(), "peer connect loop ended, took in millis: " + duration);

                long waitInSeconds = (this.reconnectIntervalInSeconds * 1000 - duration) / 1000;
                if (waitInSeconds < 1) {
                    // start again right now
                    again = true;
                    Log.writeLog(this, this.toString(), "reconnect right now again.");
                } else {
                    // make a break but setup an alarm before
                    Log.writeLog(this, this.toString(), "make a break (seconds): " + waitInSeconds);
                    this.reconnectAlarm = new AlarmClock(waitInSeconds, this);
                }
            }
        } catch (IOException e) {
            Log.writeLogErr(this, this.toString(), "fatal problems: " + e.getLocalizedMessage());
        }
        Log.writeLog(this, this.toString(), "connection manager thread ended.");
    }

    private String myString = null;
    public String toString() {
        if(this.myString == null) {
            this.myString = this.asapPeer.getPeerID().toString();
        }
        return myString;
    }
}
