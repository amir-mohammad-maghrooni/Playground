package Hub;


import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import modules.TestModule;
import modules.TestModule2;
import utils.ProjectModule;

//example of module imports
// import modules.QuantumModule;


public class ModulePageController {
    @FXML
    private GridPane moduleGrid;

    @FXML
    private javafx.scene.control.Label pageLabel;
    // Current page index for pagination

    private int currentPage=0;

    public void initialize() {
        loadModules();
    }

    private void loadModules() {
        List<ProjectModule> modules = List.of(
            // Add your module instances here
            // new QuantumModule(),
            // new AnotherModule(),
            // new YetAnotherModule()   
            new TestModule(),
            new TestModule2() // Example module, replace with actual modules
        );

        moduleGrid.getChildren().clear();

        int start = currentPage *4;
        int end = Math.min(start + 4, modules.size());

        for (int i = start; i < end; i++) {
            ProjectModule mod = modules.get(i);
            Button button = new Button(mod.getModuleName());
            button.setPrefSize(150, 100);
            button.setOnAction(e -> mod.launch());
            moduleGrid.add(button, (i % 2), (i / 2)); // Place buttons in a 2-column grid
        }
        pageLabel.setText("Page " + (currentPage + 1) + " of " + ((modules.size() + 3) / 4));
        // Update the page label to show current page and total pages
    }

    @FXML
    private void goNext() {
        currentPage++;
        loadModules();
    }

    @FXML
    private void goBack() {
        if (currentPage > 0) currentPage--;
        loadModules();
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

}