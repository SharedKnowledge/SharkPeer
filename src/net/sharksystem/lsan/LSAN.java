package net.sharksystem.lsan;

import net.sharksystem.ASAPFormats;
import net.sharksystem.SharkComponent;
import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPEncounterManagerAdmin;
import net.sharksystem.asap.ASAPPeer;

import java.io.IOException;
import java.util.*;

@ASAPFormats(formats = {LSAN.APP_FORMAT})
public interface LSAN extends SharkComponent {
    String APP_FORMAT = "lsan://formatA";
    String APP_FORMAT_MIME = "application/x-lsan";
//    Set<ASAPPeer> knowPeers = new HashSet<>();
List<ASAPPeer> knowPeers = new ArrayList<>();
int c=0;
HashMap<CharSequence,Boolean> isVisited=new HashMap<>();

    HashMap<CharSequence,LSANImpl> nodes=new HashMap<>();


    void addEncounterManagerAdmin(ASAPEncounterManagerAdmin emAdmin);
}

