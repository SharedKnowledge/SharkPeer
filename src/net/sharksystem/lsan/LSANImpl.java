package net.sharksystem.lsan;

import net.sharksystem.SharkException;
import net.sharksystem.asap.*;

import java.io.IOException;
import java.util.List;

public class LSANImpl implements LSAN, ASAPMessageReceivedListener {
    private ASAPPeer asapPeer;
    private ASAPEncounterManagerAdmin emAdmin;

    @Override
    public void onStart(ASAPPeer peer) throws SharkException {
        System.out.println("LSAN component started");
        // do something useful

        // remember this peer
        this.asapPeer = peer;

        // listen to message type A
        this.asapPeer.addASAPMessageReceivedListener(
                LSAN.APP_FORMAT, this);
    }

    @Override
    public void addEncounterManagerAdmin(ASAPEncounterManagerAdmin emAdmin) {
        this.emAdmin = emAdmin;
    }

    @Override
    public void asapMessagesReceived(ASAPMessages asapMessages, String s, List<ASAPHop> list) throws IOException {
        // TODO. Example can be found in YourComponentImpl in test folder
    }
}
