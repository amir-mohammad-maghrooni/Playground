package Hub;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HubApp extends Application {
    private static Stage primaryStage;

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("HubApp started");
        
        HubApp.primaryStage = primaryStage;
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/hub/MainMenu.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setTitle("Playground Hub");
        primaryStage.setScene(scene);
        primaryStage.show();
        MusicPlayer.startMusic(); // Start background music
    }

    public static void main(String[] args) {
        launch(args);
    }
}