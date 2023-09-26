package net.sharksystem.lsan;

import net.sharksystem.ASAPFormats;
import net.sharksystem.SharkComponent;
import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPEncounterManagerAdmin;

import java.io.IOException;
import java.util.Iterator;

@ASAPFormats(formats = {LSAN.APP_FORMAT})
public interface LSAN extends SharkComponent {
    String APP_FORMAT = "lsan://formatA";
    String APP_FORMAT_MIME = "application/x-lsan";

    void addEncounterManagerAdmin(ASAPEncounterManagerAdmin emAdmin);
}

