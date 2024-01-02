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
   Set<ASAPPeer> network = new HashSet<>();
//List<ASAPPeer> knowPeers = new ArrayList<>();
    HashMap<CharSequence,Boolean> boolList=new HashMap<>();

ASAPPeer admin = null;
    HashMap<CharSequence,LSANImpl> nodes=new HashMap<>();


    void addEncounterManagerAdmin(ASAPEncounterManagerAdmin emAdmin);
}

