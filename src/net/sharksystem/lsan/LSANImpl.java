package net.sharksystem.lsan;

import net.sharksystem.SharkException;
import net.sharksystem.asap.*;
import net.sharksystem.asap.apps.testsupport.ASAPTestPeerFS;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

public class LSANImpl implements LSAN, ASAPMessageReceivedListener,ASAPEnvironmentChangesListener {
    private ASAPPeer asapPeer;
    List<ASAPPeer> onlineDevices = new ArrayList<>();
    CharSequence adminValue="";
    private ASAPEncounterManagerAdmin emAdmin;
    private int noConnection = 0;

    @Override
    public void onStart(ASAPPeer peer) throws SharkException {
        System.out.println("welcome new LSAN component");
//        knowPeers.add(peer);
        if(peer.toString().equals("Alice")){
            nodes.put("Alice_42",this);
            isVisited.put("Alice_42",false);
        }
        else {
            nodes.put(peer.toString(), this);
            isVisited.put(peer.toString(), false);

        }
        System.out.println("the charset is "+knowPeers.toString() );
        System.out.println("NODES ARE : "+ nodes);
        // do something useful

        // remember this peer
        this.asapPeer = peer;
        this.adminValue=this.asapPeer.toString();
//        try{
//            if(adminValue.equals(knowPeers.get(0).toString())){
//                this.asapPeer.putExtra(adminValue,"true".getBytes());
//                byte[] valueBack=this.asapPeer.getExtra(adminValue);
//                String s=new String(valueBack);
//                System.out.println("THIS IS ADMIN "+s);
//            }
//            else{
//                this.asapPeer.putExtra(adminValue,"false".getBytes());
//                byte[] valueBack=this.asapPeer.getExtra(adminValue);
//                String s=new String(valueBack);
//                System.out.println("THIS IS NOT ADMIN "+s);
//            }
//
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
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
        System.out.println(this.emAdmin.getConnectedPeerIDs().toString());
    }

    @Override
    public void asapMessagesReceived(ASAPMessages asapMessages, String s, List<ASAPHop> list) throws IOException {
        // TODO. Example can be found in YourComponentImpl in test folder
    }

    // map (name -> id)
    // map (id -> name)


    @Override
    public void onlinePeersChanged(Set<CharSequence> peerList) {
        noConnection++;
        System.out.println(this.asapPeer.toString()+" HAS "+noConnection+" CONNECTIONS");
//        System.out.println("ALERT");
//        System.out.println(this.emAdmin.getConnectedPeerIDs().toString());
////         peer list has changed - maybe there is a new peer around
//        for(CharSequence maybeNewPeerName : peerList) {
//            System.out.println("PEERLIST IS: "+peerList.toString());
//            CharSequence newPeerName = maybeNewPeerName;
//            for (ASAPPeer peerName : knowPeers) {
//                if(maybeNewPeerName.toString().equalsIgnoreCase(peerName.toString())) {
//                    newPeerName = null; // not new
//                    System.out.println("NOT A NEW PEER");
//                    break; // found in my known peers list, try next in peerList
//                }
//            }
//            if(newPeerName != null) {
//                knowPeers.add((ASAPPeer) maybeNewPeerName);
//                // found one - enough for this example
//                System.out.println("new peeer has joined: "+newPeerName);;
//                System.out.println(knowPeers);
//                break;
//            }
//        }

//   System.out.println("PEER List is "+ peerList.toString());
        if(noConnection>1){
            isVisited.replaceAll((k, v) -> false);
            removeCyclic("Alice_42","-1");
        }


        
//
////        System.out.println("FINISHED");
//        System.out.println(this.asapPeer+" is connected to"+this.emAdmin.getConnectedPeerIDs().toString());
//        System.out.println(nodes.get(knowPeers.get(0).toString()).emAdmin.getConnectedPeerIDs().toString());
//
//        System.out.println("that is it ");
    }

    public synchronized void removeCyclic (CharSequence curr, CharSequence prev){
        System.out.println("IN REMOVE CYCLIC, TURN IS ON "+ curr);
        CharSequence[] connected= (CharSequence[]) nodes.get(curr).emAdmin.getConnectedPeerIDs().toArray(new CharSequence[0]);
        System.out.println("CONNECTED PEERS TO "+curr+" ARE"+Arrays.toString(connected));
        if (isVisited.get(curr)&&!prev.equals("-1")) {
            // remove edge(dont know what to write in the arguments of the cancel connection)
//            this.emAdmin.cancelConnection("David");

            System.out.println(prev+"SHOULD CANCEL CONNECTION TO "+ curr);
            // You can add the removeEdge functionality here
            return;
        }
        isVisited.put(curr,true);
//        System.out.println(connected);
        for (CharSequence child : connected) {
            System.out.println("CHILD IS: "+ child);
            System.out.println("PREV IS: "+ prev);

            System.out.println(!child.equals(prev));
            if (!child.equals(prev)) {
                removeCyclic(child, curr);
            }
        }

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
