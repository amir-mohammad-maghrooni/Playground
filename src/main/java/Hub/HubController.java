package Hub;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class HubController {
    @FXML
    @SuppressWarnings("unused")
    private Button startButton;

    @FXML
    @SuppressWarnings("unused")
    private void goToModulePage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hub/ModuleGrid.fxml"));
            Parent root = loader.load();
            Stage stage = HubApp.getPrimaryStage();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void goToSettings(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/hub/Settings.fxml"));
            Parent root = loader.load();
            Stage stage = HubApp.getPrimaryStage();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    @SuppressWarnings("unused")
    private void exitApp(ActionEvent event) {
        System.exit(0);
    }
}
