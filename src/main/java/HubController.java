import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

/**
 * HubController wires the FXML views to logic. It owns the navigation
 * between Start page and the Projects page, and delegates persistence
 * to ProjectManager.
 */
public class HubController {
    // === Start Page ===
    @FXML
    private BorderPane startRoot;
    @FXML
    private Label titleLabel;
    @FXML
    private Label subtitleLabel;
    @FXML
    private Button startButton;
    @FXML
    private Button exitButton;

    // === Projects Page ===
    @FXML
    private BorderPane projectsRoot;
    @FXML
    private GridPane projectGrid; // 3x3 grid
    @FXML
    private Button prevPageBtn;
    @FXML
    private Button nextPageBtn;
    @FXML
    private Label pageInfoLabel;
    @FXML
    private Button addProjectBtn;
    @FXML
    private Button mainMenuBtn;

    private final ObservableList<ProjectModule> modules = FXCollections.observableArrayList();
    private int currentPage = 0; // zero-based
    private static final int PAGE_SIZE = 9; // 3x3 grid

    private final ProjectManager projectManager = ProjectManager.getInstance();

    @FXML
    public void initialize() {
        // Load persisted modules on startup
        modules.setAll(projectManager.loadModules());
        setupHover(startButton);
        setupHover(exitButton);
        updateGrid();
    }

    // Utility to apply a subtle hover style by toggling a CSS class
    private void setupHover(Button b) {
        b.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> b.getStyleClass().add("hovered"));
        b.addEventHandler(MouseEvent.MOUSE_EXITED, e -> b.getStyleClass().remove("hovered"));
    }

    // =============== Start Page Actions ===============
    public void onStart(ActionEvent e) {
        startRoot.setVisible(false);
        projectsRoot.setVisible(true);
        updateGrid();
    }

    public void onExit(ActionEvent e) {
        // Save before exit for safety
        projectManager.saveModules(new ArrayList<>(modules));
        Platform.exit();
    }

    // =============== Projects Page Actions ===============
    public void onPrevPage(ActionEvent e) {
        if (currentPage > 0) {
            currentPage--;
            updateGrid();
        }
    }

    public void onNextPage(ActionEvent e) {
        int totalPages = (int) Math.ceil((double) modules.size() / PAGE_SIZE);
        if (currentPage < Math.max(0, totalPages - 1)) {
            currentPage++;
            updateGrid();
        }
    }

    public void onMainMenu(ActionEvent e) {
        projectsRoot.setVisible(false);
        startRoot.setVisible(true);
    }

    public void onAddProject(ActionEvent e) {
        // Show support info first
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Supported Formats");
        info.setHeaderText("This demo supports: .exe, .java, .bat, .html, .jar");
        info.setContentText("Pick any of these file types to add it to your Playground.");
        info.showAndWait();

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Add Project File");
        // Filter for supported types
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Supported (*.exe, *.java, *.bat, *.html, *.jar)",
                "*.exe", "*.java", "*.bat", "*.html", "*.jar"));

        // Initial directory logic: try ./Modules, else project dir
        Path modulesDir = Path.of("Modules");
        if (Files.exists(modulesDir) && Files.isDirectory(modulesDir)) {
            chooser.setInitialDirectory(modulesDir.toFile());
        } else {
            chooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        }

        File file = chooser.showOpenDialog(((Node) e.getSource()).getScene().getWindow());
        if (file == null)
            return;

        // Ask for name and optional description
        Dialog<ProjectModule> dialog = createAddDialog(file);
        dialog.showAndWait().ifPresent(pm -> {
            modules.add(pm);
            projectManager.saveModules(new ArrayList<>(modules));
            updateGrid();
        });
    }

    private Dialog<ProjectModule> createAddDialog(File file) {
        Dialog<ProjectModule> dialog = new Dialog<>();
        dialog.setTitle("New Project");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = new TextField(stripExtension(file.getName()));
        TextArea descField = new TextArea();
        descField.setPromptText("Optional description...");
        descField.setPrefRowCount(3);

        GridPane gp = new GridPane();
        gp.setHgap(10);
        gp.setVgap(10);
        gp.setPadding(new Insets(10));
        gp.add(new Label("Name:"), 0, 0);
        gp.add(nameField, 1, 0);
        gp.add(new Label("Description:"), 0, 1);
        gp.add(descField, 1, 1);
        dialog.getDialogPane().setContent(gp);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                ProjectType type = ProjectType.fromPath(file.toPath());
                return new ExternalProjectModule(nameField.getText().trim(), descField.getText().trim(),
                        file.getAbsolutePath(), type);
            }
            return null;
        });
        return dialog;
    }

    private String stripExtension(String name) {
        int i = name.lastIndexOf('.');
        return (i > 0) ? name.substring(0, i) : name;
    }

    // Rebuilds the 3x3 grid and pagination label
    private void updateGrid() {
        projectGrid.getChildren().clear();
        projectGrid.getRowConstraints().clear();
        projectGrid.getColumnConstraints().clear();

        // Configure 3x3 grid sizing
        for (int c = 0; c < 3; c++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(33.33);
            projectGrid.getColumnConstraints().add(cc);
        }
        for (int r = 0; r < 3; r++) {
            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(33.33);
            projectGrid.getRowConstraints().add(rc);
        }

        int start = currentPage * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, modules.size());
        List<ProjectModule> page = modules.subList(start, end);

        int idx = 0;
        for (ProjectModule pm : page) {
            int row = idx / 3;
            int col = idx % 3;
            projectGrid.add(createProjectCard(pm), col, row);
            idx++;
        }

        int totalPages = Math.max(1, (int) Math.ceil((double) modules.size() / PAGE_SIZE));
        pageInfoLabel.setText("Page " + (currentPage + 1) + " / " + totalPages);

        // Enable/disable page buttons
        prevPageBtn.setDisable(currentPage == 0);
        nextPageBtn.setDisable(currentPage >= totalPages - 1);
    }

    // Builds a VBox containing: big launch button + red remove button
    private VBox createProjectCard(ProjectModule pm) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(10));
        box.getStyleClass().add("card");
        box.setAlignment(Pos.TOP_CENTER);

        Button launch = new Button();
        launch.setMaxWidth(Double.MAX_VALUE);
        launch.setMinHeight(120);
        launch.getStyleClass().add("launch-btn");
        setupHover(launch);
        String text = pm.getName();
        if (pm.getDescription() != null && !pm.getDescription().isBlank()) {
            text += "\n\n" + pm.getDescription();
        }
        launch.setText(text);
        launch.setOnAction(ev -> pm.launchNewWindow());

        Button remove = new Button("Remove");
        remove.getStyleClass().add("remove-btn");
        remove.setOnAction(ev -> {
            modules.remove(pm);
            projectManager.saveModules(new ArrayList<>(modules));
            updateGrid();
        });

        VBox.setVgrow(launch, Priority.ALWAYS);
        box.getChildren().addAll(launch, remove);
        return box;
    }
}