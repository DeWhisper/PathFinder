import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.imageio.ImageIO;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class PathFinder extends Application{

    //Scene height and width
    private final int width = 620;
    private final int startHeight = 70;

    //Instances
    private ListGraph<Location> listGraph = new ListGraph<>();
    private ArrayList<Location> selectedLocations = new ArrayList<>();

    //Visual
    private List<Circle> circles = new ArrayList<>();
    private List<Line> lines = new ArrayList<>();
    private List<Text> texts = new ArrayList<>();

    private Pane center = new Pane();
    private Stage stage;
    private Scene scene;

    private MenuBar menuBar;
    private BorderPane root;
   
    private Button findPath;
    private Button showConnection;
    private Button newPlace; 
    private Button newConnection; 
    private Button changeConnection;
    
    //Files
    private Image image;
    private ImageView imageView = new ImageView();
    private String imageUrlString = "europa.gif";
    private File file = new File("europa.graph");

    //Unsaved Changes boolean
    private boolean unsavedChanges = false;
    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage stage) {
        
        this.stage = stage;
        stage.setTitle("PathFinder");
        root = new BorderPane();
        // Skapa en menyfält (MenuBar) och lägg till menyalternativ
        menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        fileMenu.setId("menuFile");
        MenuItem newMapMenu = new MenuItem("New Map");
        MenuItem openMenu = new MenuItem("Open");
        MenuItem saveMenu = new MenuItem("Save");
        MenuItem saveImageMenu = new MenuItem("Save Image");
        MenuItem exitMenu = new MenuItem("Exit");
        fileMenu.getItems().addAll(newMapMenu, openMenu, saveMenu, saveImageMenu, exitMenu);
        menuBar.getMenus().add(fileMenu);
    
        // Placera menyn längst upp i BorderPane
        root.setTop(menuBar);
    
        //Background image
        try (
        BufferedReader reader = new BufferedReader(new FileReader(file))) {
        imageUrlString = reader.readLine();
        } catch (IOException e){
            e.printStackTrace();
        }
        
    
        // Skapa en HBox för knapparna
        HBox box = new HBox(
            findPath = new Button("Find Path"),
            showConnection = new Button("Show Connection"),
            newPlace = new Button("New Place"),
            newConnection = new Button("New Connection"),
            changeConnection= new Button("Change Connection")
        );
        box.setSpacing(10);
        box.setPadding(new Insets(10,10,10,10));
        box.setAlignment(Pos.CENTER);

        //-------------ActionEvents MenuBar-------------//

        saveImageMenu.setOnAction(event -> screenshot());
        newMapMenu.setOnAction(event -> newMap()); 
        openMenu.setOnAction(event -> {
            try {
                open();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        saveMenu.setOnAction(event -> save());
        exitMenu.setOnAction(event -> exit());

        //-------------ActionEvents HBox-------------//
        
        findPath.setOnAction(event -> findPath());
        showConnection.setOnAction(event -> showConnection());
        newPlace.setOnAction(event -> changeCrosshair());
        newConnection.setOnAction(event -> newConnection());
        changeConnection.setOnAction(event -> changeConnection());

    
        // Placera HBox under bilden i BorderPane
        root.setBottom(center);
        root.setCenter(box);
    
        //ImageView
        center.getChildren().add(imageView);

        // Scene initilization
        scene = new Scene(root, width, startHeight);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();

         //ID's
         menuBar.setId("menu");
         newMapMenu.setId("menuNewMap");
         openMenu.setId("menuOpenFile");
         saveMenu.setId("menuSaveFile");
         saveImageMenu.setId("menuSaveImage");
         exitMenu.setId("menuExit");
         center.setId("outputArea");
 
         // Set IDs for Buttons
         findPath.setId("btnFindPath");
         showConnection.setId("btnShowConnection");
         newPlace.setId("btnNewPlace");
         changeConnection.setId("btnChangeConnection");
         newConnection.setId("btnNewConnection");
 
    }
    private void newMap(){
        if(unsavedChanges){
            if(continuePopup() != ButtonType.OK){
                return;
            }
        }
        image = new Image(imageUrlString);
        imageView.setImage(image);
        imageView.setViewOrder(2);
        //Stage
        stage.setHeight(startHeight + image.getHeight());
        removeConnections();
        unsavedChanges = true;
    }
    
    private void open() throws Exception{
        if(unsavedChanges){
            if(continuePopup() != ButtonType.OK){
                return;
            }
        }
        if (!file.exists()) {
            error("File " + file.toString() + " does not exist!");
            return;
        }
        //Remove all the nodes and conenctions
        removeConnections();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String mapName = reader.readLine();

        //IMAGE
        imageUrlString = mapName;
        image = new Image(imageUrlString);
        imageView.setImage(image);
        imageView.setViewOrder(2);
        //center.getChildren().clear();

        //SCENE
        stage.setHeight(startHeight + image.getHeight());

        String line = reader.readLine();
        String[] locationSplit = line.split(";");
        for (int i = 0; i < locationSplit.length; i += 3) {
            String name = locationSplit[i];
            double x = Double.parseDouble(locationSplit[i + 1]);
            double y = Double.parseDouble(locationSplit[i + 2]);
            Location location = new Location(name, x, y);
            listGraph.add(location);
            createLocation(name, x, y);
        }
        while((line = reader.readLine()) != null){
            String[] edgeSplit = line.split(";");
            String fromName = edgeSplit[0];
            String toName = edgeSplit[1];
            String transportName = edgeSplit[2];
            int weight = Integer.parseInt(edgeSplit[3]);
            Location from = findLocation(listGraph.getNodes(), fromName);
            Location to = findLocation(listGraph.getNodes(), toName);
            //Connection
            if(from != null && to != null && listGraph.getEdgeBetween(from, to) == null){
                listGraph.connect(from, to, transportName, weight);
                createLine(to);
            }
        }
        //UNSAVED CHANGES
        unsavedChanges = true;

        //System.out.println(listGraph.toString());
        reader.close();
    }
    private void save(){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(imageUrlString);
            writer.newLine();
            for(Location location : listGraph.getNodes()){
                writer.write(location.toString());
            }
            writer.newLine();
            for (Location location : listGraph.getNodes()){
                Set<Edge<Location>> edgeList = listGraph.getEdgesFrom(location);
                for (Edge<Location> edge : edgeList) {
                    writer.write(String.format("%s;%s;%s;%d",location.getName(), edge.getDestination().getName(), edge.getName(), edge.getWeight()));
                    writer.newLine();
                }
            }
            writer.close();
            unsavedChanges = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void exit(){
        if(unsavedChanges){
            if(continuePopup() != ButtonType.OK){
                return;
            }
        }
        stage.close();
    }
    private void changeCrosshair(){
        newPlace.setDisable(true);
        center.setCursor(Cursor.CROSSHAIR);
        center.setOnMouseClicked(event -> newLocation(event.getX(), event.getY()));
    }
    
    private void newLocation(double x, double y){
        //System.out.println("X: " + x + " Y: " + y);
        center.setOnMouseClicked(null);
        center.setCursor(Cursor.DEFAULT);

        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Name");
        dialog.setHeaderText(null);
        dialog.setContentText("Name of place");

        TextField textField = new TextField();
        dialog.getDialogPane().setContent(textField); 

        Optional<ButtonType> result = dialog.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK) {
            String placeName = textField.getText();
            createLocation(placeName, x, y);
            unsavedChanges = true;
        }
        newPlace.setDisable(false);
    }

    private void findPath(){
        if(selectedLocations.size() != 2){
            error();
            return;
        }
        Location from = selectedLocations.get(0);
        Location to = selectedLocations.get(1);
        if(!listGraph.pathExists(from, to)){
            error("No path between locations!");
        } else{
            TextArea textArea = new TextArea();
            textArea.setEditable(false);
            List<Edge<Location>> path = listGraph.getPath(from, to);
            int weightCounter = 0; 
            for(Edge<Location> edge : path){
                textArea.appendText(String.format("to %s by %s takes %d\n", edge.getDestination().getName(), edge.getName(), edge.getWeight()));
                weightCounter += edge.getWeight();
            }
            textArea.appendText("Total " + weightCounter);


            Alert alertbox = new Alert(Alert.AlertType.INFORMATION);
            alertbox.getDialogPane().setContent(textArea);
            alertbox.setTitle("Message");
            alertbox.setContentText(null);
            alertbox.setHeaderText("The Path from " + from.getName() + " to " + to.getName() + ":");
            alertbox.showAndWait();  
        } 
    }

    private void showConnection(){
        if(selectedLocations.size() != 2){
            error();
            return;
        }
        Location from = selectedLocations.get(0);
        Location to = selectedLocations.get(1);
        
        if(listGraph.getEdgeBetween(from, to) == null){
            error("No edges between nodes!");
        }
        TextField nameField = new TextField();
        TextField timeField = new TextField();
        nameField.setEditable(false);
        timeField.setEditable(false);

        Edge<Location> e = listGraph.getEdgeBetween(to, from);
        if(e != null){
            nameField.setText(e.getName());
            timeField.setText(String.valueOf(e.getWeight()));

            Alert alertbox = new Alert(Alert.AlertType.CONFIRMATION);
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(5);
            grid.setPadding(new Insets(10));
            grid.addRow(0, new Label("Name: "), nameField);
            grid.addRow(1, new Label("Time: "), timeField);
            alertbox.getDialogPane().setContent(grid);
            alertbox.setTitle("Connection");
            alertbox.setHeaderText("Connection from " + from.getName() + " to " + to.getName());
            alertbox.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
            alertbox.showAndWait();
        }
    }
    
    private void newConnection(){
        if(selectedLocations.size() != 2){
            error();
        } else{
            Location from = selectedLocations.get(0);
            Location to = selectedLocations.get(1);
            if(from == null || to == null){
                error("From or To is null");
                return;
            }
            if(listGraph.getEdgeBetween(from, to) != null){
                error("There already exist a connection!");
                return;
            }
            
            TextField nameField = new TextField();
            TextField timeField = new TextField();

            Alert alertbox = new Alert(Alert.AlertType.CONFIRMATION);
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(5);
            grid.setPadding(new Insets(10));
            grid.addRow(0, new Label("Name: "), nameField);
            grid.addRow(1, new Label("Time: "), timeField);
            alertbox.getDialogPane().setContent(grid);
            alertbox.setTitle("Connection");
            alertbox.setHeaderText(null);

            //Confirm or cancel input
            alertbox.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
            Optional<ButtonType> result = alertbox.showAndWait();
            if(result.isPresent() && result.get() == ButtonType.OK){
                if(!nameField.getText().isEmpty() && !timeField.getText().isEmpty()){
                    listGraph.connect(from, to, nameField.getText(), Integer.valueOf(timeField.getText()));
                    createLine(selectedLocations.get(0));
                }else{
                    error("Invalid input!");
                }
            }
            unsavedChanges = true;
        }
    }
    
    private void changeConnection() {
        if(selectedLocations.size() != 2){
            error();
            return;
        }
        Location from = selectedLocations.get(0);
        Location to = selectedLocations.get(1);
        
        if(listGraph.getEdgeBetween(from, to) == null){
            error("No edges between nodes!");
        }
        TextField nameField = new TextField();
        TextField timeField = new TextField();
        nameField.setEditable(false);
        Edge<Location> e = listGraph.getEdgeBetween(to, from);
        if(e != null){
            nameField.setText(e.getName());

            Alert alertbox = new Alert(Alert.AlertType.CONFIRMATION);
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(5);
            grid.setPadding(new Insets(10));
            grid.addRow(0, new Label("Name: "), nameField);
            grid.addRow(1, new Label("Time: "), timeField);
            alertbox.getDialogPane().setContent(grid);
            alertbox.setTitle("Connection");
            alertbox.setHeaderText("Connection from " + from.getName() + " to " + to.getName());
            alertbox.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
            Optional<ButtonType> result = alertbox.showAndWait();

            if(result.isPresent() && result.get() == ButtonType.OK){
                if(!timeField.getText().isEmpty()){
                    listGraph.setConnectionWeight(from, to, Integer.valueOf(timeField.getText()));
                    //System.out.println(timeField.getText());
                }
            }
            unsavedChanges = true;
        }
    }
    //----------------------Help methods -------------------------//
    private Location findLocation(Set<Location> locations, String name){
        for(Location location : locations){
            if(location.getName().equals(name)){
                return location;
            }
        }
        return null;
    }

    private void screenshot(){
        WritableImage image = center.snapshot(null, null);
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        try {
            ImageIO.write(bufferedImage, "png", new File("capture.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createLocation(String name, double x, double y){
        Location location = new Location(name, x, y);
        listGraph.add(location);
        Text text = new Text(name);
        text.setLayoutX(x + 10);
        text.setLayoutY(y + 10);
        center.getChildren().addAll(text);
        text.setFont(Font.font(15));
        //Add to array
        texts.add(text);
        location.setViewOrder(0);
        center.getChildren().add(location);
        circles.add(location);
        //Event for location click
        location.setOnMouseClicked(event ->{
            if(location.getFill() == Color.RED){
                location.setFill(Color.BLUE);
                selectedLocations.remove(location);
            }else{
                if(selectedLocations.size() < 2){
                    location.setFill(Color.RED);
                    selectedLocations.add(location);
                }
            } 
        });
    }
    private void removeConnections(){
        for (Location city : listGraph.getNodes()) {
            listGraph.remove(city);
        }
        for(Circle circle : circles){
            center.getChildren().remove(circle);
        }
        for(Line line : lines){
            center.getChildren().remove(line);
        }
        for(Text text : texts){
            center.getChildren().remove(text);
        }
        selectedLocations.clear();
    }

    private void createLine(Location location){
        Set<Edge<Location>> edges = listGraph.getEdgesFrom(location);
        for(Edge<Location> edge : edges){
            Location destination = edge.getDestination();
            Line line = new Line(location.getCenterX(), location.getCenterY(), destination.getCenterX(), destination.getCenterY());
            line.setViewOrder(1);
            center.getChildren().add(line);
            lines.add(line);
        } 
        return;
    }

    private void error(String text){
        Alert msgBox = new Alert(Alert.AlertType.ERROR );
        msgBox.setTitle("Error");
        msgBox.setHeaderText(null);
        msgBox.setContentText(text);
        msgBox.showAndWait();
    }
    
    private void error(){
        Alert msgBox = new Alert(Alert.AlertType.ERROR );
        msgBox.setTitle("Error");
        msgBox.setHeaderText(null);
        msgBox.setContentText("Two places must be selected!");
        msgBox.showAndWait();
    }

    private ButtonType continuePopup() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Warning!");
        alert.setHeaderText(null);
        alert.setContentText("Unsaved changes, continue anyways?");
        alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        Optional<ButtonType> result = alert.showAndWait();
        return result.get();
    }
}