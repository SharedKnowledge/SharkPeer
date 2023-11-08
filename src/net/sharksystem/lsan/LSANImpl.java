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

//    private byte[] isAdmin=new byte[knowPeers.size()+1];
    CharSequence adminValue="";
    private ASAPEncounterManagerAdmin emAdmin;

    @Override
    public void onStart(ASAPPeer peer) throws SharkException {
        System.out.println("welcom new LSAN component");
        knowPeers.add(peer);
//        isAdmin[0]=true;
        System.out.println();

        System.out.println("the charset is "+knowPeers.toString() );
        System.out.println("First PEER IN THE NETWORK IS"+ knowPeers.get(0).toString());
        // do something useful

        // remember this peer
        this.asapPeer = peer;
        this.adminValue=this.asapPeer.toString();
        try{
            if(adminValue.equals(knowPeers.get(0).toString())){
                this.asapPeer.putExtra(adminValue,"true".getBytes());
                byte[] valueBack=this.asapPeer.getExtra(adminValue);
                String s=new String(valueBack);
                System.out.println("THIS IS ADMIN "+s);
            }
            else{
                this.asapPeer.putExtra(adminValue,"false".getBytes());
                byte[] valueBack=this.asapPeer.getExtra(adminValue);
                String s=new String(valueBack);
                System.out.println("THIS IS NOT ADMIN "+s);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
    }

    @Override
    public void asapMessagesReceived(ASAPMessages asapMessages, String s, List<ASAPHop> list) throws IOException {
        // TODO. Example can be found in YourComponentImpl in test folder
    }

    public void removeCyclic (){
System.out.println("IN REMOVE CYCLIC");
this.emAdmin.cancelConnection("David");

    }

    @Override
    public void onlinePeersChanged(Set<CharSequence> peerList) {
        // peer list has changed - maybe there is a new peer around
//        for(CharSequence maybeNewPeerName : peerList) {
//            System.out.println("PEERLIST IS: "+peerList.toString());
//            CharSequence newPeerName = maybeNewPeerName;
//            for (ASAPPeer peerName : this.knowPeers) {
//                if(maybeNewPeerName.toString().equalsIgnoreCase(peerName.toString())) {
//                    newPeerName = null; // not new
//                    System.out.println("NOT A NEW PEER");
//                    break; // found in my known peers list, try next in peerList
//                }
//            }
//            if(newPeerName != null) {
//                // found one - enough for this example
//                this.doSomethingWith(newPeerName); // example
//                break;
//            }
//        }

//        try {
//            this.asapPeer.putExtra(adminValue,isAdmin);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } catch (ASAPException e) {
//            throw new RuntimeException(e);
//        }
        byte[] valueBack= new byte[0];
        try {
            valueBack = this.asapPeer.getExtra(this.adminValue);
        } catch (ASAPException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String s=new String(valueBack);
        System.out.println("CONNECTION CHANGE, ADMIN STATUS: "+ s);
        if(s.equals("true")){
            this.removeCyclic();
        }
        else{
            System.out.println("DONT EXECUTE PROTOCOL");
        }
        System.out.println("THERE IS A CHANGE");
        System.out.println(this.asapPeer+" is connected to"+this.emAdmin.getConnectedPeerIDs().toString());
        System.out.println("that is it ");
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
