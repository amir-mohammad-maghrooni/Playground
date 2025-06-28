package Hub;

import java.util.prefs.Preferences;

import javafx.application.Application;
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
        String savedRes = prefs.get("resolution", "800x600");
        double savedVol = prefs.getDouble("volume", 0.5);

        MusicPlayer.setVolume(savedVol);
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/hub/MainMenu.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/Styles.css").toExternalForm());

        if ("Fullscreen".equals(savedRes)) {
            primaryStage.setFullScreen(true);
        } else {
            String[] parts = savedRes.split("x");
            primaryStage.setWidth(Integer.parseInt(parts[0]));
            primaryStage.setHeight(Integer.parseInt(parts[1]));
        }


        primaryStage.setTitle("Playground Hub");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        MusicPlayer.startMusic(); // Start background music
    }

    public static void main(String[] args) {
        launch(args);
    }
}