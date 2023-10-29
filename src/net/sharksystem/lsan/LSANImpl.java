package net.sharksystem.lsan;

import net.sharksystem.SharkException;
import net.sharksystem.asap.*;
import net.sharksystem.asap.apps.testsupport.ASAPTestPeerFS;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LSANImpl implements LSAN, ASAPMessageReceivedListener,ASAPEnvironmentChangesListener {
    private ASAPPeer asapPeer;
    private ASAPEncounterManagerAdmin emAdmin;

    @Override
    public void onStart(ASAPPeer peer) throws SharkException {
        System.out.println("welcom new LSAN component");
        knowPeers.add(peer);

        System.out.println("the charset is "+knowPeers.toString() );
        // do something useful

        // remember this peer
        this.asapPeer = peer;

        //listen for changing connections
        this.asapPeer.addASAPEnvironmentChangesListener(this);
        // listen to message type A
        this.asapPeer.addASAPMessageReceivedListener(
                LSAN.APP_FORMAT, this);
    }

    @Override
    public void addEncounterManagerAdmin(ASAPEncounterManagerAdmin emAdmin) {
        this.emAdmin = emAdmin;
        System.out.println("CONNECTED");
        System.out.println(emAdmin.getConnectedPeerIDs().toString());
    }

    @Override
    public void asapMessagesReceived(ASAPMessages asapMessages, String s, List<ASAPHop> list) throws IOException {
        // TODO. Example can be found in YourComponentImpl in test folder
    }

    public void removeCyclic (){

    }

    @Override
    public void onlinePeersChanged(Set<CharSequence> peerList) {
        // peer list has changed - maybe there is a new peer around
//        for(CharSequence maybeNewPeerName : peerList) {
//            CharSequence newPeerName = maybeNewPeerName;
//            for (CharSequence peerName : this.knowPeers) {
//                if(maybeNewPeerName.toString().equalsIgnoreCase(peerName.toString())) {
//                    newPeerName = null; // not new
//                    break; // found in my known peers list, try next in peerList
//                }
//            }
//            if(newPeerName != null) {
//                // found one - enough for this example
//                this.doSomethingWith(newPeerName); // example
//                break;
//            }
//        }
        System.out.println("THERE IS A CHANGE");
    }

    private void doSomethingWith(CharSequence newPeerName) {
        // create a uri
        CharSequence uri = "yourApp://" + newPeerName + "_AND_" + this.asapPeer.getPeerID() + "_haveAChat";

        try {
            // create a PDU of your applications - example
            byte[] yourMessage = this.serializeYourPDU(newPeerName);
            // send a message to any peer - recipient is in your protocol data unit
            this.asapPeer.sendASAPMessage(LSAN.APP_FORMAT, uri, yourMessage);
        } catch (ASAPException | IOException e) {
            System.out.println("problems: " + e.getLocalizedMessage());
        }
    }


    private byte[] serializeYourPDU(CharSequence newPeerName) throws IOException {
        // just an example
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream daos = new DataOutputStream(baos);

        // write time
        daos.writeLong(System.currentTimeMillis());
        // write local name
        daos.writeUTF(this.asapPeer.getPeerID().toString());
        // write recipient name
        daos.writeUTF(newPeerName.toString());
        daos.writeUTF("Hi there.");

        return baos.toByteArray();
    }
}
