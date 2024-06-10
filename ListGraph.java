import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class ListGraph<T> implements Graph<T>, Serializable {

    private Map<T, Set<Edge<T>>> adjacencyList;


    public ListGraph(){
        this.adjacencyList = new HashMap<>();
    }
    
    public void add(T node){
        adjacencyList.putIfAbsent(node, new HashSet<>());
    }
    
    public void remove(T node){
        if(!adjacencyList.containsKey(node)){
            throw new NoSuchElementException();
        }
        //Ny remove
        for(Set<Edge<T>> edges : adjacencyList.values()){
            edges.removeIf(edge -> edge.getDestination().equals(node));
        }
        adjacencyList.remove(node);
    }

    public void connect(T node1, T node2, String connectionName, int weight){
        if(!adjacencyList.containsKey(node1) || !adjacencyList.containsKey(node2)){
            throw new NoSuchElementException();
        }
        if(weight < 0){
            throw new IllegalArgumentException();
        }

        Edge<T> edge = getEdgeBetween(node1, node2);
        if(edge != null){
            throw new IllegalStateException("Node1: "+ node1 + " Node2: " + node2);
        }
        
        Edge<T> edge1 = new Edge<>(node2, connectionName, weight);
        Edge<T> edge2 = new Edge<>(node1, connectionName, weight);
        adjacencyList.get(node1).add(edge1);
        adjacencyList.get(node2).add(edge2);
    }

    public void disconnect(T node1, T node2){
        if(!adjacencyList.containsKey(node1) || !adjacencyList.containsKey(node2)){
            throw new NoSuchElementException();
        } 
        Edge<T> edgeToRemove = getEdgeBetween(node1, node2);
        if(edgeToRemove == null){
            throw new IllegalStateException();
        }
        adjacencyList.get(node1).remove(edgeToRemove);
        //Ändrade till removeif
        adjacencyList.get(node2).removeIf(edge -> edge.getDestination().equals(node1));
    }

    public void setConnectionWeight(T node1, T node2, int weight) {
        if(!adjacencyList.containsKey(node1) || !adjacencyList.containsKey(node2)){
            throw new NoSuchElementException();
        }
        if(weight < 0){
            throw new IllegalArgumentException();
        }
        Edge<T> edge = getEdgeBetween(node1, node2);
        if(edge == null){
            throw new IllegalStateException();
        }
        Edge<T> edge1 = getEdgeBetween(node1, node2);
        Edge<T> edge2 = getEdgeBetween(node2, node1);
        
        edge1.setWeight(weight);
        edge2.setWeight(weight);
    }
    
    public Set<T> getNodes(){
        return new HashSet<>(adjacencyList.keySet());
    }
    
    public Set<Edge<T>> getEdgesFrom(T node){
        if(!adjacencyList.containsKey(node)){
            throw new NoSuchElementException();
        }
        return new HashSet<>(adjacencyList.get(node));
    }

    public Edge<T> getEdgeBetween(T node1, T node2){
        if(!adjacencyList.containsKey(node1) || !adjacencyList.containsKey(node2)){
            throw new NoSuchElementException(node2.toString() +
            adjacencyList.keySet().toString());
        }
        Set<Edge<T>> edgesFromNode = adjacencyList.get(node1);
        for(Edge<T> edge : edgesFromNode){
            if(edge.getDestination().equals(node2)){
                return edge;
            }
        }
        return null;
    }

    public boolean pathExists(T node1, T node2){
        if(!adjacencyList.containsKey(node1) || !adjacencyList.containsKey(node2)){
            return false;
        }
        Set<T> visited = new HashSet<>();
        depthFirstVisitAll(node1, visited);
        return visited.contains(node2);
    }
    private void depthFirstVisitAll(T currentNode, Set<T> visited){
        visited.add(currentNode);
        for(Edge<T> edge : adjacencyList.get(currentNode)){
            if(!visited.contains(edge.getDestination())){
                depthFirstVisitAll(edge.getDestination(), visited);
            }
        }
    }
    
    public List<Edge<T>> getPath(T from, T to) {
        Map<T, T> connection = new HashMap<>();
        depthFirstConnection(from, null, connection);
        List<Edge<T>> path = new ArrayList<>();
        while (connection.get(to) != null) {
            Edge<T> edge = getEdgeBetween(connection.get(to), to);
            path.add(edge);
            to = connection.get(to);
        }
        Collections.reverse(path);
        return path.isEmpty() ? null : path;
    }

    private void depthFirstConnection(T to, T from, Map<T, T> connection){
        connection.put(to, from);
        for(Edge<T> edge : adjacencyList.get(to)){
            if(!connection.containsKey(edge.getDestination())){
                depthFirstConnection(edge.getDestination(), to, connection);
            }
        }
    }
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        for(Map.Entry<T,Set<Edge<T>>> entry: adjacencyList.entrySet()){
            T node = entry.getKey();
            Set<Edge<T>> edges = entry.getValue();
            stringBuilder.append(node.toString()).append("\n");
            
            for(Edge<T> edge : edges){
                stringBuilder.append(" ").append(edge).append("\n");
            }
        }
        return stringBuilder.toString();
    }
}