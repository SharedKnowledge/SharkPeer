package net.sharksystem.lsan;

import net.sharksystem.SharkException;
import net.sharksystem.asap.*;
import net.sharksystem.asap.apps.testsupport.ASAPTestPeerFS;

import java.io.*;
import java.util.*;

public class LSANImpl implements LSAN, ASAPMessageReceivedListener,ASAPEnvironmentChangesListener {
    private ASAPPeer asapPeer;
    List<ASAPPeer> onlineDevices = new ArrayList<>();
    CharSequence adminValue="";
    private ASAPEncounterManagerAdmin emAdmin;

    HashMap<CharSequence,Boolean> isVisited=new HashMap<>();
    private Set<CharSequence> knowPeers = new HashSet<>();

    private String result="";
    private Random random = new Random();
    private int randomNumber=-1;

//    List<String> redundant= new ArrayList<>();
    private int noConnection = 0;

    @Override
    public void onStart(ASAPPeer peer) throws SharkException {
        System.out.println("welcome new LSAN component");

//        knowPeers.add(peer);
        network.add(peer);
        // loop through the knowpeers list and with each loop add the peer to the is visited hashmap

        if(peer.toString().equals("Alice")){
            nodes.put("Alice_42",this);
            boolList.put("Alice_42",false);
        }
        else {
            nodes.put(peer.toString(), this);
            boolList.put(peer.toString(),false);
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
        this.isVisited=boolList;
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
    public void asapMessagesReceived(ASAPMessages msgs,
                                     String senderE2E, List<ASAPHop> hops) throws IOException {
        System.out.println(this.asapPeer.toString()+" YOU RECEIVED A MSG");
        System.out.println(msgs.getFormat() + " | " + msgs.getURI() +  " | " + hops);
        Iterator<byte[]> yourPDUIter = msgs.getMessages(); // access messages
        while(yourPDUIter.hasNext()) {
            byte[] yourAppMessage = yourPDUIter.next();
            this.deserialize(yourAppMessage);
        }
    }
//    public void asapMessagesReceived(ASAPMessages messages,
//                                     String senderE2E, // E2E part
//                                     List<ASAPHop> asapHop) throws IOException {
//        Iterator<byte[]> msgIter = messages.getMessages();
//
//        // you could check uri, e.g. to figure out what chat is addressed, what running game, what POS offering...
//        CharSequence uri = messages.getURI();
//        System.out.println("got messages ( uri | number ): (" + uri + " | " + messages.size() + ")");
//
//        // if uri fits - you could do something with the content - your serialized data
//        while(msgIter.hasNext()) {
//            byte[] yourAppMessage = msgIter.next();
//            this.deserialize(yourAppMessage);
//        }
//    }


    // map (id -> name)


    @Override
    public void onlinePeersChanged(Set<CharSequence> peerList) {
        System.out.println(this.asapPeer.toString() + ", YOU GOT A NEW NOTIFICATION");
        noConnection=this.emAdmin.getConnectedPeerIDs().size();
        System.out.println("current peerlist of "+this.asapPeer.toString()+" is"+peerList.toString());
        System.out.println("Known peers "+this.knowPeers.toString());
        for(CharSequence maybeNewPeerName : peerList) {
            CharSequence newPeerName = maybeNewPeerName;
            for (CharSequence peerName :this.knowPeers) {
                if(maybeNewPeerName.toString().equalsIgnoreCase(peerName.toString())) {
                    newPeerName = null; // not new
                    break; // found in my known peers list, try next in peerList
                }
            }
            if(newPeerName != null) {
                // found one - enough for this example
//                System.out.println(newPeerName +"joined time to broadcst to others");
                knowPeers.add(newPeerName);
//                try {
//                    this.broadcast(newPeerName); // example
//                } catch (ASAPException e) {
//                    throw new RuntimeException(e);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
                if(noConnection>1){
                    System.out.println(this.asapPeer.toString()+" has new connection, execute the protocol");
                    if(this.asapPeer.toString().equals("Alice")){
                        this.isVisited.replaceAll((k, v) -> false);
                        this.removeCyclic("Alice_42","-1");
                        System.out.println(this.asapPeer.toString()+" result is "+ this.result);
                        this.randomNumber=random.nextInt(100)+1;
                        this.doSomethingWith(newPeerName);
                    }

                    else{
                        this.isVisited.replaceAll((k, v) -> false);
                         this.result=this.removeCyclic(this.asapPeer.toString(),"-1");
                         if(result.isEmpty())
                             System.out.println("no result");
                         else{
                             this.randomNumber=random.nextInt(100)+1;
                             this.doSomethingWith(newPeerName);
                             System.out.println(this.asapPeer.toString()+" result is "+ this.result);
                         }

                    }
                }

//                break;
            }
        }
//        System.out.println("NEW NOTFICATION");
//
//        System.out.println(this.asapPeer.toString()+" HAS "+noConnection+" CONNECTIONS");
//
//
//            System.out.println("FUNCTION CALLED");
//            this.isVisited.replaceAll((k, v) -> false);
//           removeCyclic(this.asapPeer.toString(),"-1");
//        if(noConnection>1){
//
//            try {
//                for(int i=0;i<knowPeers.size();i++){
//                    if(!knowPeers.get(i).toString().equals(this.asapPeer.toString())){
//                        broadcast(knowPeers.get(i).toString());
//                    }
//                }
//
//            } catch (ASAPException | IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
        //        System.out.println(this.isVisited);
//removeCyclic(this.asapPeer.toString(),"-1");
    }

//    private void broadcastNewConnection() throws ASAPException, IOException {
//        CharSequence uri = "yourApp://"+this.asapPeer;
//        CharSequence message = "New connection: " + this.asapPeer.toString();
//        byte[] yourMessage = this.serializeYourPDU(message);
//        // Send broadcast message to all known peers
//                this.asapPeer.sendASAPMessage(LSAN.APP_FORMAT, uri, yourMessage);
//
//    }
    public synchronized String removeCyclic (CharSequence curr, CharSequence prev){
        System.out.println("IN REMOVE CYCLIC, TURN IS ON "+ curr);
        CharSequence[] connected= (CharSequence[]) nodes.get(curr).emAdmin.getConnectedPeerIDs().toArray(new CharSequence[0]);
        System.out.println("CONNECTED PEERS TO "+curr+" ARE"+Arrays.toString(connected));
        if (this.isVisited.get(curr)&&!prev.toString().equals("-1")) {
            // remove edge(dont know what to write in the arguments of the cancel connection)
//            this.emAdmin.cancelConnection("David");

            System.out.println(prev+"SHOULD CANCEL CONNECTION TO "+ curr);
//            nodes.get(prev).emAdmin.cancelConnection(curr);
//            nodes.get(curr).emAdmin.cancelConnection(prev);
////            nodes.get(curr).emAdmin.getConnectedPeerIDs().remove(prev);
//            nodes.get(prev).emAdmin.getConnectedPeerIDs().remove(curr);
//
//            nodes.get(curr).emAdmin.cancelConnection(prev);
            // You can add the removeEdge functionality here
//            this.redundant.add(prev.toString()+'-'+curr.toString());
            this.result+=prev.toString()+"-"+curr.toString();
//            System.out.println("Redundant connections in "+this.asapPeer.toString()+" prespective are: "+this.redundant);
            return result;
        }

            this.isVisited.put(curr,true);
            System.out.println("visited list of "+this.asapPeer.toString()+" is "+this.isVisited);
//        System.out.println(connected);
            for (CharSequence child : connected) {
                System.out.println("CHILD IS: "+ child);
                System.out.println("PREV IS: "+ prev);

                if (!child.toString().equals(prev.toString())) {
                        return this.result+removeCyclic(child, curr);
                }
            }

        return result;
    }



    private void doSomethingWith(CharSequence newPeerName) {
        // create a uri
        CharSequence uri = "yourApp://" + newPeerName + "_AND" +
                "_" + this.asapPeer.getPeerID();

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
        //write redundant connection the sender node found
        daos.writeUTF(this.result);
        //write generated random number
    daos.writeUTF(String.valueOf(this.randomNumber));
        return baos.toByteArray();
    }


    private void deserialize(byte[] yourAppMessage) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(yourAppMessage);
        DataInputStream dis = new DataInputStream(bais);
//
//
//
//

        // it is an example
        StringBuilder sb = new StringBuilder();
        sb.append("received message was created: ").append(new Date(dis.readLong()));
        sb.append("\n");

        sb.append("iphone: ").append(this.asapPeer.toString());
        sb.append("\n");
        String sender=dis.readUTF();
        sb.append("sender: ").append(sender);
        sb.append(" | ");

        // you could decide to ignore or handle this message based on recipient
        String recipient=dis.readUTF();
        sb.append("recipient: ").append(recipient);
        sb.append("\n");

        if (!this.asapPeer.toString().equals(recipient)) {
            System.out.println("Ignoring message. Current node is not the recipient.");
            return; // Skip further processing
        }
            String redundant=dis.readUTF();
        sb.append("message: ").append(redundant);

        sb.append("\n");
        String number=dis.readUTF();
        sb.append("number: ").append(number);

       System.out.println(sb);
       if(this.randomNumber>Integer.parseInt(number)){
           System.out.println("my connection will be cancelled");
           initiateCancel(this.result);
       }
       else{
           System.out.println("my neighbour node's connections will be cancelled");
       }
    }
    public void initiateCancel(String result){
       String [] cancel= result.split("-");
       ASAPPeer first=null;
        ASAPPeer second=null;
//        for (CharSequence key : nodes.keySet()) {
//            if (key.toString().equals(cancel[0])) {
//                LSANImpl value = nodes.get(key);
//                // Perform your operations on the LSANImpl value associated with the key
//                value.emAdmin.cancelConnection(cancel[1]);
//                // Add your operations here
//                return; // If you only want to perform the operation on the first matching key
//            }
//        }
        nodes.get(cancel[0]).emAdmin.cancelConnection(cancel[1]);

    }
}
