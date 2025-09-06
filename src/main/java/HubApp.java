import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * HubApp bootstraps the JavaFX application and loads the start screen.
 * All UI is defined via FXML and connected to HubController.
 */
public class HubApp extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/hub.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setTitle("Playground");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}