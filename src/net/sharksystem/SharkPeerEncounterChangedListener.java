package net.sharksystem;

public interface SharkPeerEncounterChangedListener {
    void encounterStarted(CharSequence peerID);
    void encounterTerminated(CharSequence peerID);
}
