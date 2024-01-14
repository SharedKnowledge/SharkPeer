package net.sharksystem.lsan;
import net.sharksystem.*;
import net.sharksystem.asap.ASAPEncounterConnectionType;
import net.sharksystem.asap.ASAPEncounterManagerImpl;
import net.sharksystem.asap.ASAPPeerFS;
import net.sharksystem.asap.apps.TCPServerSocketAcceptor;
import net.sharksystem.utils.streams.StreamPairImpl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class CustomGraph {
    String[] connections;
    Map<String, ASAPEncounterManagerImpl> nodes = new HashMap<String, ASAPEncounterManagerImpl>();
    private static final String LOCALHOST = "localhost";


    public CustomGraph(String[] connections){
        this.connections = connections;
    }

    private void setupNode(String nodeName, ArrayList<String> createdNodes) throws SharkException, IOException, InterruptedException{
        if(!createdNodes.contains(nodeName)){
            String nodeFolder = TestHelper.getUniqueFolderName("sharkComponent/" + nodeName);
            net.sharksystem.utils.testsupport.TestHelper.incrementTestNumber();
            SharkTestPeerFS.removeFolder(nodeFolder);
            SharkTestPeerFS sharkPeer = new SharkTestPeerFS(nodeName, nodeFolder);
            YourComponent component = TestHelper.setupComponent(sharkPeer);
            ExampleYourComponentListener listener = new ExampleYourComponentListener();
            component.subscribeYourComponentListener(listener);

            sharkPeer.addComponent(new LSANFactory(), LSAN.class);
            LSAN lsan = (LSAN) sharkPeer.getComponent(LSAN.class);
            ASAPPeerFS asapPeerFS = new ASAPPeerFS(nodeName, nodeFolder, sharkPeer.getSupportedFormats());
            sharkPeer.start(asapPeerFS);
            ASAPEncounterManagerImpl encounterManager = new ASAPEncounterManagerImpl(asapPeerFS, nodeName);
            lsan.addEncounterManagerAdmin(encounterManager);

            createdNodes.add(nodeName);
            nodes.put(nodeName, encounterManager);
        }

    }

    public void setupGraph() throws SharkException, IOException, InterruptedException {
        ArrayList<String> createdNodes = new ArrayList<>();
        for(String connection : connections){
            String[] nodeNames = connection.split("-");

            setupNode(nodeNames[0], createdNodes);
            setupNode(nodeNames[1], createdNodes);
        }
        connectGraph();
    }

    private void connectGraph() throws SharkException, IOException, InterruptedException {
        for(String connection : connections){
            String[] nodeNames = connection.split("-");
            // connect
            int port = TestHelper.getPortNumber();
            new TCPServerSocketAcceptor(port, nodes.get(nodeNames[0]));
            Socket connect2Node = new Socket(LOCALHOST, port);
            nodes.get(nodeNames[1]).handleEncounter(
                    StreamPairImpl.getStreamPair(
                            connect2Node.getInputStream(), connect2Node.getOutputStream(), nodes.get(nodeNames[0]).toString(), nodes.get(nodeNames[0]).toString()),
                    ASAPEncounterConnectionType.INTERNET);

        }
    }
}
