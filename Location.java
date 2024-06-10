import java.util.Objects;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Location extends Circle{


    public Location(String name, double x, double y){
        super(x, y, 10);
        this.setId(name);
        this.setFill(Color.BLUE);
    }
    
    public String getName() {
        return getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o instanceof Location) {
            Location location = (Location) o;
            return Double.compare(location.getCenterX(), getCenterX()) == 0 &&
                   Double.compare(location.getCenterY(), getCenterY()) == 0 &&
                   Objects.equals(getId(), location.getId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getCenterX(), getCenterY());
    }

    @Override
    public String toString(){
        return String.format("%s;%s;%s;", getId(), String.valueOf(getCenterX()) , String.valueOf(getCenterY()));
    }
}