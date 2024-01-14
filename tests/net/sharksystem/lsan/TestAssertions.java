package net.sharksystem.lsan;

import net.sharksystem.asap.ASAPEncounterManagerImpl;

import java.util.*;

import org.junit.Assert;

public class TestAssertions {
    private Map<String, ASAPEncounterManagerImpl> nodes;
    int maxConnections;

//    public TestAssertions(Map<String, ASAPEncounterManagerImpl> nodes, int maxConnections){
//        this.nodes = nodes;
//        this.maxConnections = maxConnections;
//    }
    public TestAssertions(CustomGraph customGraph, int maxConnections){
        this.nodes = customGraph.nodes;
        this.maxConnections = maxConnections;
    }
    public TestAssertions(PatternGraph patternGraph, int maxConnections){
        this.nodes = patternGraph.nodes;
        this.maxConnections = maxConnections;
    }

    private boolean isConnected(String nodeName1, String nodeName2){
        ASAPEncounterManagerImpl node1 = nodes.get(nodeName1);
        ASAPEncounterManagerImpl node2 = nodes.get(nodeName2);

        return node1.getConnectedPeerIDs().contains(node2.toString());
    }
    private boolean canAcceptConnections(String nodeName){
        ASAPEncounterManagerImpl node = nodes.get(nodeName);

        return node.getConnectedPeerIDs().size() < maxConnections;
    }
    private boolean hasNeighbors(String nodeName){
        ASAPEncounterManagerImpl node = nodes.get(nodeName);

        return !node.getConnectedPeerIDs().isEmpty();
    }
    private boolean hasTheseNeighbors(String nodeName, String neighbors){
        String[] neighborNames = neighbors.split(",");
        for(int i =0; i< neighborNames.length; i++){
            if(!isConnected(nodeName, neighborNames[i])){
                return false;
            }
        }
        return true;
    }
    private boolean isGraphConnected(){
        Set<String> visitedNodes = new HashSet<>();
        String startNode = nodes.keySet().iterator().next();
        dfs(startNode, visitedNodes);

        return visitedNodes.size() == nodes.size();
    }
    private void dfs(String currentNode, Set<String> visitedNodes){
        visitedNodes.add(currentNode);
        Set<CharSequence> connectedNodes = nodes.get(currentNode).getConnectedPeerIDs();

        for(CharSequence connectedNode : connectedNodes){
            String connectedNodeString = connectedNode.toString();
            if(!visitedNodes.contains(connectedNodeString)){
                dfs(connectedNodeString, visitedNodes);
            }
        }
    }

    private boolean isCyclic(){
        // Set to keep track of visited nodes during DFS traversal
        Set<String> visitedNodes = new HashSet<>();
        // First node in the graph to start the DFS traversal
        String startNode = nodes.keySet().iterator().next();
        return dfsCyclic(startNode, visitedNodes, "-1");
    }

    private boolean dfsCyclic(String currentNode, Set<String> visitedNodes, String parentNode){
        // Mark the current node as visited
        visitedNodes.add(currentNode);
        // Get current node's neighbors
        Set<CharSequence> connectedNodes = nodes.get(currentNode).getConnectedPeerIDs();
        boolean result = false;
        // Explore each neighbor
        for(CharSequence connectedNode : connectedNodes){
            String connectedNodeString = connectedNode.toString();
            // Check for cycles
            if(!parentNode.equals(connectedNode.toString())) {
                if (visitedNodes.contains(connectedNodeString)) {
                    System.out.println("ok`1`````````````````````````````");
                    System.out.println(currentNode+" "+ connectedNode+" "+parentNode);
                    return true; // Cycle detected
                }
                else{
                    // Recursively explore connected nodes and update result
                    result |= dfsCyclic(connectedNode.toString(), visitedNodes, currentNode);
                }
            }
        }
        return result;
    }
    private int nodeDegree(String nodeName){
        ASAPEncounterManagerImpl node = nodes.get(nodeName);

        return node.getConnectedPeerIDs().size();
    }
    private double averageNoOfAvailableConnections(){
        Set<String> visitedNodes = new HashSet<>();
        String startNode = nodes.keySet().iterator().next();
        int ans = dfsAvailableConnections(startNode, visitedNodes);
        return (double)ans / nodes.size();
    }
    private int dfsAvailableConnections(String currentNode, Set<String> visitedNodes){
        visitedNodes.add(currentNode);
        Set<CharSequence> connectedNodes = nodes.get(currentNode).getConnectedPeerIDs();
        int sum = maxConnections - connectedNodes.size();

        for(CharSequence connectedNode : connectedNodes){
            String connectedNodeString = connectedNode.toString();
            if(!visitedNodes.contains(connectedNodeString)){
                sum += dfsAvailableConnections(connectedNodeString, visitedNodes);
            }
        }
        return sum;
    }
    private int shortestPathLength(String startNodeName, String endNodeName) {
        ASAPEncounterManagerImpl startNode = nodes.get(startNodeName);
        ASAPEncounterManagerImpl endNode = nodes.get(endNodeName);

//        if (startNode == null || endNode == null) {
//            return -1; // Return -1 or throw an exception if start or end node does not exist
//        }

        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        Map<String, Integer> distance = new HashMap<>();

        queue.offer(startNodeName);
        visited.add(startNodeName);
        distance.put(startNodeName, 0);

        while (!queue.isEmpty()) {
            String currentNode = queue.poll();
            int currentDistance = distance.get(currentNode);

            if (currentNode.equals(endNodeName)) {
                return currentDistance;
            }

            for (CharSequence neighbor : nodes.get(currentNode).getConnectedPeerIDs()) {
                String neighborStr = neighbor.toString();
                if (!visited.contains(neighborStr)) {
                    visited.add(neighborStr);
                    queue.offer(neighborStr);
                    distance.put(neighborStr, currentDistance + 1);
                }
            }

        }
        return -1; // If no path exists
    }
    private boolean exactNumberOfEdgesCutOff(List<String> edges, int desiredCount){
        int cutOffCount = 0;

        for(String edge : edges){
            String[] nodeNames = edge.split("-");
            if(!isConnected(nodeNames[0], nodeNames[1])){
                cutOffCount++;
            }
        }
        if(cutOffCount == desiredCount){
            return true;
        }else{
            return false;
        }
    }

    private int numberOfEdges(){
        int sum = 0;
        for(ASAPEncounterManagerImpl node : nodes.values()){
            sum += node.getConnectedPeerIDs().size();
        }
        return sum/2;
    }

    private String findArticulationPoints() {
        ArrayList<String> articulationPoints = new ArrayList<>();
        int time = 0;
        Map<String, Boolean> visited = new HashMap<>();
        Map<String, Integer> discTime = new HashMap<>();
        Map<String, Integer> lowTime = new HashMap<>();
        Map<String, String> parent = new HashMap<>();

        for (String nodeName : nodes.keySet()) {
            if (!visited.containsKey(nodeName)) {
                dfsArticulation(nodeName, visited, discTime, lowTime, parent, articulationPoints, time);
            }
        }

        return String.join(",", articulationPoints);
    }

    private void dfsArticulation(String currentNode, Map<String, Boolean> visited, Map<String, Integer> discTime, Map<String, Integer> lowTime, Map<String, String> parent, ArrayList<String> articulationPoints, int time) {
        visited.put(currentNode, true);
        discTime.put(currentNode, time);
        lowTime.put(currentNode, time);
        int childCount = 0;
        boolean isArticulation = false;

        for (CharSequence neighbor : nodes.get(currentNode).getConnectedPeerIDs()) {
            String neighborStr = neighbor.toString();

            if (!visited.containsKey(neighborStr)) {
                childCount++;
                parent.put(neighborStr, currentNode);
                dfsArticulation(neighborStr, visited, discTime, lowTime, parent, articulationPoints, time + 1);

                if (lowTime.get(neighborStr) >= discTime.get(currentNode)) {
                    isArticulation = true;
                }

                lowTime.put(currentNode, Math.min(lowTime.get(currentNode), lowTime.get(neighborStr)));
            } else if (!neighborStr.equals(parent.get(currentNode))) {
                lowTime.put(currentNode, Math.min(lowTime.get(currentNode), discTime.get(neighborStr)));
            }
        }

        if ((parent.get(currentNode) == null && childCount >= 2) || (parent.get(currentNode) != null && isArticulation)) {
            articulationPoints.add(currentNode);
        }
    }

    private boolean isBiConnected(){
        String articulationPoints = findArticulationPoints();

        return articulationPoints.isEmpty();
    }

    private boolean isStarNode(String nodeName){
        ASAPEncounterManagerImpl node = nodes.get(nodeName);

        return numberOfEdges() == nodes.size()-1;
    }

    private boolean possibleArticulationPoints(String[] possibleArticulationPoints) {
        String actualArticulationPoints = findArticulationPoints();
        String[] actualPointsArray = actualArticulationPoints.split(",");
        Arrays.sort(actualPointsArray);

        for (String possiblePoint : possibleArticulationPoints) {
            String[] possiblePointArray = possiblePoint.split(",");
            Arrays.sort(possiblePointArray);
            if (Arrays.equals(possiblePointArray, actualPointsArray)) {
                return true;
            }
        }

        return false;
    }




    public void assertNodeConnected(String nodeName1, String nodeName2){
        Assert.assertTrue("Nodes " + nodeName1 + " and " + nodeName2 + " should be connected.", isConnected(nodeName1, nodeName2));
    }
    public void assertNodeNotConnected(String nodeName1, String nodeName2){
        Assert.assertFalse("Nodes " + nodeName1 + " and " + nodeName2 + " should not be connected.", isConnected(nodeName1, nodeName2));
    }
    public void assertNodeCanAcceptConnections(String nodeName){
        Assert.assertTrue("Node " + nodeName + " should be able to accept more connections.", canAcceptConnections(nodeName));
    }
    public void assertNodeCannotAcceptConnections(String nodeName){
        Assert.assertFalse("Node " + nodeName + " should not be able to accept more connections.", canAcceptConnections(nodeName));
    }
    public void assertNodeHasNeighbors(String nodeName){
        Assert.assertTrue(hasNeighbors(nodeName));
    }
    public void assertNodeHasNoNeighbors(String nodeName){
        Assert.assertFalse(hasNeighbors(nodeName));
    }

    public void assertNodeHasTheseNeighbors(String nodeName, String neighbors){
        Assert.assertTrue("Node " + nodeName + " should have these neighbors: " + neighbors, hasTheseNeighbors(nodeName, neighbors));
    }
    public void assertNodeDoesNotHaveTheseNeighbors(String nodeName, String neighbors){
        Assert.assertFalse("Node " + nodeName + " should not have these neighbors: " + neighbors,hasTheseNeighbors(nodeName, neighbors));
    }

    public void assertGraphIsConnected(){
        Assert.assertTrue("Graph should be connected.", isGraphConnected());
    }
    public void assertGraphIsNotConnected(){
        Assert.assertFalse("Graph should not be connected.", isGraphConnected());
    }

    public void assertGraphIsCyclic(){
        Assert.assertTrue("Graph should be cyclic.", isCyclic());
    }
    public void assertGraphIsAcyclic(){
        Assert.assertFalse("Graph should be acyclic.", isCyclic());
    }

    public void assertEqualDegree(String nodeName, int degree){
        Assert.assertEquals("Node " + nodeName + " should have a degree of " + degree, degree, nodeDegree(nodeName));
    }
    public void assertNotEqualDegree(String nodeName, int degree){
        Assert.assertNotEquals("Node " + nodeName + " should not have a degree of " + degree, degree, nodeDegree(nodeName));
    }
    public void assertEqualAvgAvailableConnections(double avg){
        Assert.assertEquals(avg, averageNoOfAvailableConnections(), 1e-6);
    }
    public void assertShortestPathLength(String nodeName1, String nodeName2, int expectedLength){
        int actualLength = shortestPathLength(nodeName1, nodeName2);
        Assert.assertEquals(expectedLength, actualLength);
    }
    public void assertEqualNumberOfEdgesCutOff(List<String> edges, int desiredCount){
        Assert.assertTrue(exactNumberOfEdgesCutOff(edges, desiredCount));
    }
    public void assertEqualNumberOfEdges(int numberOfEdges){
        Assert.assertEquals(numberOfEdges, numberOfEdges());
    }

    public void assertEqualArticulationPoints(String points){
        String[] expectedPointsArray = points.split(",");
        String[] actualPointsArray = findArticulationPoints().split(",");

        Arrays.sort(expectedPointsArray);
        Arrays.sort(actualPointsArray);

        Assert.assertArrayEquals(expectedPointsArray, actualPointsArray);
    }
    public void assertGraphIsBiConnected(){
        Assert.assertTrue(isBiConnected());
    }
    public void assertGraphIsNotBiConnected(){
        Assert.assertFalse(isBiConnected());
    }
    public void assertStarNode(String nodeName){
        Assert.assertTrue(isStarNode(nodeName));
    }
    public void assertNotStarNode(String nodeName){
        Assert.assertFalse(isStarNode(nodeName));
    }
    public void assertPossibleArticulationPoints(String[] articulationPoints){
        Assert.assertTrue(possibleArticulationPoints(articulationPoints));
    }


}