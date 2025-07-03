package Hub;

import java.util.prefs.Preferences;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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

        Preferences prefs = Preferences.userNodeForPackage(SettingsController.class);
        double savedVol = prefs.getDouble("volume", 0.5);

        MusicPlayer.setVolume(savedVol);
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/hub/MainMenu.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/Styles.css").toExternalForm());

        primaryStage.setWidth(800);
        primaryStage.setHeight(600);
        primaryStage.setResizable(false);

        primaryStage.setTitle("Playground Hub");
        primaryStage.setScene(scene);
        
        // Handle proper shutdown
        primaryStage.setOnCloseRequest(e -> {
            // Stop music and clean up resources
            MusicPlayer.stopMusic();
            
            // Ensure JavaFX shuts down properly
            Platform.exit();
            
            // Force system exit if needed
            System.exit(0);
        });
        
        primaryStage.show();

        MusicPlayer.startMusic(); // Start background music
    }

    @Override
    public void stop() throws Exception {
        // This is called when the application is shutting down
        System.out.println("HubApp stopping...");
        MusicPlayer.stopMusic();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}