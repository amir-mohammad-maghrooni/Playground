import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 * ExternalProjectModule implements launching logic for different types.
 * It opens a new Stage per launch, and either embeds a WebView (for HTML)
 * or runs a Process (for .jar/.java/.bat/.exe), streaming stdout/stderr
 * into a TextArea.
 */
public class ExternalProjectModule extends ProjectModule {

    public ExternalProjectModule(String name, String description, String path, ProjectType type) {
        super(name, description, path, type);
    }

    @Override
    public void launchNewWindow() {
        switch (getType()) {
            case HTML -> openHtmlWindow();
            case JAR -> launchProcessWindow(new String[] { "java", "-jar", getPath() });
            case JAVA -> launchProcessWindow(new String[] { "java", getPath() }); // Java 11+ source-file mode
            case BAT, EXE -> launchProcessWindow(new String[] { getPath() });
            default -> showError("Unsupported type: " + getType());
        }
    }

    private void openHtmlWindow() {
        Stage stage = new Stage();
        stage.setTitle(getName() + " (HTML)");
        WebView web = new WebView();
        web.getEngine().load(Path.of(getPath()).toUri().toString());
        stage.setScene(new Scene(web, 1000, 700));
        stage.show();
    }

    private void launchProcessWindow(String[] command) {
        Stage stage = new Stage();
        stage.setTitle(getName() + " (Process)");
        TextArea console = new TextArea();
        console.setEditable(false);
        console.setWrapText(true);
        stage.setScene(new Scene(console, 900, 600));
        stage.show();

        Thread runner = new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(command);
                // Set working directory to file's parent
                File f = new File(getPath());
                if (f.getParentFile() != null)
                    pb.directory(f.getParentFile());
                pb.redirectErrorStream(true);
                Process p = pb.start();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String ln = line + "\n";
                        Platform.runLater(() -> console.appendText(ln));
                    }
                }
                int code = p.waitFor();
                Platform.runLater(() -> console.appendText("\n[Process exited with code " + code + "]\n"));
            } catch (IOException | InterruptedException ex) {
                Platform.runLater(() -> showError("Failed to launch: " + ex.getMessage()));
            }
        }, "ProcessRunner");
        runner.setDaemon(true);
        runner.start();
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText("Launch Error");
        a.setContentText(msg);
        a.showAndWait();
    }
}