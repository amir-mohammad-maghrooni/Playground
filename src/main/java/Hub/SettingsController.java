package Hub;

import java.util.prefs.Preferences;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.stage.Stage;

public class SettingsController {
    @FXML 
    private ComboBox<String> resolutionBox;

    @FXML
    private Slider volumeSlider;

    private final Preferences perfs = Preferences.userNodeForPackage(SettingsController.class);



    @FXML
    public void initialize() {
        resolutionBox.getItems().addAll("800x600", "1024x768", "1280x720", "1920x1080", "Fullscreen");
        
        // Restore previous resolution setting if exists
        String savedRes = perfs.get("resolution", null);
        if (savedRes != null) {
            resolutionBox.setValue(savedRes);  // Set AFTER adding listener
        }
    
        resolutionBox.setOnAction(e -> {
            String selected = resolutionBox.getValue();
            if (selected != null && !selected.isEmpty()) {
                applyResolution();
            }
        });
    
        volumeSlider.setValue(perfs.getDouble("volume", 0.5));
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            perfs.putDouble("volume", newVal.doubleValue());
            MusicPlayer.setVolume(newVal.doubleValue());
        });
    }



    private void applyResolution() {
        Stage stage = HubApp.getPrimaryStage();
        if (stage == null) {
            System.err.println("Primary stage is not set yet!");
            return;
        }

        String selected = resolutionBox.getValue();
        perfs.put("resolution", selected);

        if ("Fullscreen".equals(selected)){
            stage.setFullScreen(true);
        } else {
            stage.setFullScreen(false);
            String[] parts = selected.split("x");
            stage.setWidth(Integer.parseInt(parts[0]));
            stage.setHeight(Integer.parseInt(parts[1]));
        }
    }   

    @FXML
    private void goToMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hub/MainMenu.fxml"));
            Scene scene = new Scene(loader.load());
            HubApp.getPrimaryStage().setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void pauseMusic() {
        MusicPlayer.pauseMusic();
    }

    @FXML
    private void resumeMusic() {
        MusicPlayer.resumeMusic();
    }

}
