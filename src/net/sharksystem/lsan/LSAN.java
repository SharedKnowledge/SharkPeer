package net.sharksystem.lsan;

import net.sharksystem.ASAPFormats;
import net.sharksystem.SharkComponent;
import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPEncounterManagerAdmin;
import net.sharksystem.asap.ASAPPeer;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@ASAPFormats(formats = {LSAN.APP_FORMAT})
public interface LSAN extends SharkComponent {
    String APP_FORMAT = "lsan://formatA";
    String APP_FORMAT_MIME = "application/x-lsan";
    Set<ASAPPeer> knowPeers = new HashSet<>();

    void addEncounterManagerAdmin(ASAPEncounterManagerAdmin emAdmin);
}

