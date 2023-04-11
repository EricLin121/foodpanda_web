package Ask_Name_Address;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 *
 * @author user
 */
public class AskFXMain extends Application {
    
    static Stage stage = new Stage();

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("AskScreen.fxml"));
        Scene scene = new Scene(root);
        this.stage = stage;
        stage.getIcons().add(new Image("/imgs_M/pandahead.png"));
        
        stage.setTitle("Name and location");
        stage.setTitle("訂購人資料介面");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    public void showWindow() throws Exception{
         start(stage);
    }
    public static void closeWindows()
    {
        stage.hide();
    }
    
}
