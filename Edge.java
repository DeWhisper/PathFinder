import java.io.Serializable;

public class Edge<T> implements Serializable {

    private T destination;
    private String name;
    private int weight;
    private Class<T> type; 

    public Edge(T destination, String name, int weight){
        this.destination = destination;
        this.name = name;
        this.weight = weight;
    }

    public T getDestination(){
        return destination; 
    }

    public int getWeight(){
        return weight;
    }
    public Class<T> getType() {
        return type;
    }
    
    public void setWeight(int weight){
        if (weight < 0){
            throw new IllegalArgumentException();
        }
        this.weight = weight;
    }
    
    public String getName(){
        return name;
    }
    @Override
    public String toString() {
        return "to " + destination.toString() + " by " + name + " takes " + weight;
    }
}
 