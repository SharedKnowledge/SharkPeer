package net.sharksystem.lsan;

import net.sharksystem.*;
import net.sharksystem.asap.ASAPEncounterConnectionType;
import net.sharksystem.asap.ASAPEncounterManagerImpl;
import net.sharksystem.asap.ASAPPeerFS;
import net.sharksystem.asap.apps.TCPServerSocketAcceptor;
import net.sharksystem.utils.streams.StreamPairImpl;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatternGraph {
    int numberOfNodes;
    ConnectionPattern connectionPattern;
    Map<String, ASAPEncounterManagerImpl> nodes = new HashMap<String, ASAPEncounterManagerImpl>();
    private static final String LOCALHOST = "localhost";


    public PatternGraph(int numberOfNodes, ConnectionPattern connectionPattern){
        this.numberOfNodes = numberOfNodes;
        this.connectionPattern = connectionPattern;
    }

    public void setupGraph() throws SharkException, IOException, InterruptedException {
        for(int i =0 ;i < numberOfNodes; i++){
            String nodeName = "node"+i;
            String nodeFolder = TestHelper.getUniqueFolderName("sharkComponent/" + i);
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

            nodes.put(nodeName, encounterManager);
        }
        connectGraph();
    }

    private void connectGraph()  throws SharkException, IOException, InterruptedException{
        String nodeName = "node";
        for(int i = 0; i< nodes.size(); i++){
            List<Integer> connections = connectionPattern.connect(i, numberOfNodes);
            for(Integer targetNodeIndex : connections){
                if (targetNodeIndex >= 0 && targetNodeIndex < numberOfNodes && targetNodeIndex != i){
                    // connect
                    int port = TestHelper.getPortNumber();
                    new TCPServerSocketAcceptor(port, nodes.get(nodeName+i));
                    Socket connect2Node = new Socket(LOCALHOST, port);
                    nodes.get(nodeName+targetNodeIndex).handleEncounter(
                            StreamPairImpl.getStreamPair(
                                    connect2Node.getInputStream(), connect2Node.getOutputStream(), nodes.get(nodeName+i).toString(), nodes.get(nodeName+i).toString()),
                            ASAPEncounterConnectionType.INTERNET);
                }
            }
        }
    }
}
