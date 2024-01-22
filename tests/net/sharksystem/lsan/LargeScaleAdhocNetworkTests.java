package net.sharksystem.lsan;

import net.sharksystem.*;
import net.sharksystem.asap.*;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

public class LargeScaleAdhocNetworkTests {

    @Test
    public void scenario1() throws SharkException, ASAPException, IOException, InterruptedException{

        // Creating scenario using CustomGraph
        String[] connections = {"A-B", "A-C", "C-D", "B-D"};
        CustomGraph graph = new CustomGraph(connections);
        graph.setupGraph();

        // Using TestAssertions on the created graph and defining maximum number of connections as 3
        TestAssertions assertions = new TestAssertions(graph, 3);

        // assuming protocol is not working
        assertions.assertGraphIsBiConnected();
        assertions.assertGraphIsConnected();
        assertions.assertGraphIsConnected();
        assertions.assertGraphIsKRegular(2);
        assertions.assertEqualNumberOfEdges(4);
        assertions.assertEqualNumberOfRedundantConnections(1);
        assertions.assertEqualAvgAvailableConnections(1);
    }

    @Test
    public void scenario2() throws SharkException, ASAPException, IOException, InterruptedException{

        // Creating scenario using PatternGraph
        PatternGraph graph = new PatternGraph(6, (nodeIndex, totalNodes) ->
                Arrays.asList((nodeIndex + 1), (nodeIndex + 2)));
        graph.setupGraph();

        // Using TestAssertions on the created graph and defining maximum number of connections as 7
        TestAssertions assertions = new TestAssertions(graph, 7);

        // Assuming protocol does not work
        assertions.assertGraphIsBiConnected();
        assertions.assertGraphIsConnected();
        assertions.assertGraphIsCyclic();
        assertions.assertEqualNumberOfEdges(9);
        assertions.assertEqualArticulationPoints("");
        assertions.assertEqualAvgAvailableConnections(4);
        assertions.assertEqualNumberOfRedundantConnections(4);
        assertions.assertNotStarGraph();
        assertions.assertEqualDegree("node0", 2);
        assertions.assertNodeHasTheseNeighbors("node0", "node1,node2");
        assertions.assertEqualDegree("node2", 4);
        assertions.assertNodeHasTheseNeighbors("node2", "node0,node1,node3,node4");
    }

}
