
package Second_buy;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;

import javafx.stage.Stage;


public class Second extends Application {

    Stage stage = new Stage();

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("Second.fxml"));
        Scene scene = new Scene(root);
        this.stage = stage;
        // set icon
        stage.getIcons().add(new Image("/imgs_M/pandahead.png"));
        
        stage.setTitle("Welcome to FoodPanda");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    public void showWindow() throws Exception{
         start(stage);
    }

}
