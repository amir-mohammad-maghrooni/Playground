package modules;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import utils.ProjectModule;

public class TestModule implements ProjectModule {
    @Override
    public String getModuleName() {
        return "This is a Test Module";
    }

    @Override
    public void launch() {
        Stage stage = new Stage();
        stage.setTitle(getModuleName());
        stage.setScene(new Scene(new Label("Test Module Loaded!"), 300, 200));
        stage.show();
    }
}