package Hub;

import java.util.prefs.Preferences;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.stage.Stage;

public class SettingsController {
    @FXML 
    private ComboBox<String> resolutionBox;

    @FXML
    private Slider volumeSlider;

    private final Preferences prefs = Preferences.userNodeForPackage(SettingsController.class);

    @FXML
    public void initialize() {
        //resolutionBox.getItems().addAll("800x600", "1024x768", "1280x720", "1920x1080", "Fullscreen");
        
        // Restore previous resolution setting if exists
        // String savedRes = prefs.get("resolution", "800x600"); // Default to 800x600 if not set
        // if (savedRes != null) {
        //     resolutionBox.setValue(savedRes);
        // }
    
        // resolutionBox.setOnAction(e -> {
        //     String selected = resolutionBox.getValue();
        //     if (selected != null && !selected.isEmpty()) {
        //         prefs.put("resolution", selected);
        //         applyResolution(selected);
        //     }
        // });
        
        double savedVol = prefs.getDouble("volume", 0.5);
        if (savedVol >= 0 && savedVol <= 1) {
            volumeSlider.setValue(savedVol);
        } else {
            volumeSlider.setValue(0.5); // Default value if saved value is out of range
        }
        
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            prefs.putDouble("volume", newVal.doubleValue());
            MusicPlayer.setVolume(newVal.doubleValue());
        });
    }

    // private void applyResolution(String selected) {
    //     Stage stage = HubApp.getPrimaryStage();
    //     if (stage == null) return; // Ensure stage is not null

    //     if ("Fullscreen".equals(selected)){
    //         stage.setFullScreen(true);
    //     } else {
    //         stage.setFullScreen(false);
    //         String[] parts = selected.split("x");
    //         stage.setWidth(Integer.parseInt(parts[0]));
    //         stage.setHeight(Integer.parseInt(parts[1]));
    //     }
    // }   

    @FXML
    @SuppressWarnings("unused")
    private void goToMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hub/MainMenu.fxml"));
            Parent root = loader.load();
            Stage stage = HubApp.getPrimaryStage();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void pauseMusic() {
        MusicPlayer.pauseMusic();
    }

    @FXML
    @SuppressWarnings("unused")
    private void resumeMusic() {
        MusicPlayer.resumeMusic();
    }

}
