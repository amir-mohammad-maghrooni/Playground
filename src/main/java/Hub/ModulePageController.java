package Hub;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import modules.TestModule;
import modules.TestModule2;
import utils.ProjectModule;

public class ModulePageController {
    @FXML
    private GridPane moduleGrid;

    @FXML
    private javafx.scene.control.Label pageLabel;
    private int currentPage = 0;
    private List<ProjectModule> userProjects = new ArrayList<>();

    public void initialize() {
        loadUserProjects();
        loadModules();
    }

    private void loadUserProjects() {
        try {
            userProjects = ProjectManager.loadProjects();
        } catch (Exception e) {
            showErrorAlert("Loading Error", "Failed to load user projects: " + e.getMessage());
            userProjects = new ArrayList<>();
        }
    }

    private void loadModules() {
        List<ProjectModule> allModules = new ArrayList<>(List.of(
            new TestModule(),
            new TestModule2()
        ));
        allModules.addAll(userProjects);

        moduleGrid.getChildren().clear();

        int start = currentPage * 4;
        int end = Math.min(start + 4, allModules.size());

        for (int i = start; i < end; i++) {
            ProjectModule mod = allModules.get(i);
            
            // Create container for the module
            VBox moduleContainer = new VBox(5);
            moduleContainer.setAlignment(Pos.CENTER);
            moduleContainer.setPrefSize(150, 120);
            
            // Main project button
            Button mainButton = new Button(mod.getModuleName());
            mainButton.setPrefSize(150, 100);   
            mainButton.setOnAction(e -> mod.launch());
            
            moduleContainer.getChildren().add(mainButton);
            
            // Add remove button for user projects only
            if (userProjects.contains(mod)) {
                Button removeBtn = new Button("✕ Remove");
                removeBtn.setPrefSize(150, 20);
                removeBtn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-font-size: 10px;");
                removeBtn.setOnAction(e -> removeProject(mod));
                moduleContainer.getChildren().add(removeBtn);
            }
            
            // Add to grid
            int row = (i - start) / 2;
            int col = (i - start) % 2;
            moduleGrid.add(moduleContainer, col, row);
        }
        pageLabel.setText("Page " + (currentPage + 1) + " of " + ((allModules.size() + 3) / 4));
    }

    /**
     * Removes a user project after confirmation
     */
    private void removeProject(ProjectModule project) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Remove Project");
        confirmDialog.setHeaderText("Remove \"" + project.getModuleName() + "\"?");
        confirmDialog.setContentText("This action cannot be undone. The project will be removed from the hub but the actual files will remain on your system.");
        
        ButtonType removeButton = new ButtonType("Remove");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmDialog.getButtonTypes().setAll(removeButton, cancelButton);
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == removeButton) {
            try {
                // Remove from ProjectManager
                ProjectManager.removeProject(project);
                
                // Reload projects and modules
                loadUserProjects();
                loadModules();
                
                // Show success message
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Project Removed");
                successAlert.setHeaderText(null);
                successAlert.setContentText("Project \"" + project.getModuleName() + "\" has been removed successfully.");
                successAlert.showAndWait();
                
            } catch (Exception e) {
                showErrorAlert("Remove Error", "Failed to remove project: " + e.getMessage());
            }
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleAddProject(){
        Alert typeDialog = new Alert(Alert.AlertType.CONFIRMATION);
        typeDialog.setTitle("Project Type");
        typeDialog.setHeaderText("Select project format");
        typeDialog.setContentText("Choose how you want to add the project:");
        
        ButtonType jarButton = new ButtonType("JAR File");
        ButtonType dirButton = new ButtonType("Project Folder");
        ButtonType mainClassButton = new ButtonType("Select Main Class File");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        typeDialog.getButtonTypes().setAll(jarButton, dirButton, mainClassButton, cancelButton);
        
        Optional<ButtonType> result = typeDialog.showAndWait();
        if (result.isPresent()) {
            if (result.get() == jarButton) {
                addJarProject();
            } else if (result.get() == dirButton) {
                addDirectoryProject();
            } else if (result.get() == mainClassButton) {
                addProjectWithMainClassSelection();
            }
        }
    }

    private void addJarProject() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Project JAR File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JAR Files", "*.jar")
        );
        File selectedFile = fileChooser.showOpenDialog(moduleGrid.getScene().getWindow());
        
        if (selectedFile != null) {
            configureAndSaveProject(selectedFile);
        }
    }

    private void addDirectoryProject() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Select Project Folder");
        File selectedDir = dirChooser.showDialog(moduleGrid.getScene().getWindow());
        
        if (selectedDir != null) {
            String suggestedMain = detectMainClass(selectedDir);
            configureAndSaveProject(selectedDir, suggestedMain);
        }
    }
    
    private void addProjectWithMainClassSelection() {
        // First select the main class file
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Main Class File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Java Files", "*.java")
        );
        File mainClassFile = fileChooser.showOpenDialog(moduleGrid.getScene().getWindow());
        
        if (mainClassFile != null) {
            // Ask for the project directory
            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle("Select Project Root Folder");
            File projectDir = dirChooser.showDialog(moduleGrid.getScene().getWindow());
            
            if (projectDir != null) {
                // Convert file path to fully qualified class name
                String className = convertFilePathToClassName(projectDir, mainClassFile);
                if (className != null) {
                    configureAndSaveProject(projectDir, className);
                }
            }
        }
    }
    
    private String convertFilePathToClassName(File projectRoot, File javaFile) {
        try {
            // Get relative path from project root to Java file
            Path projectPath = projectRoot.toPath();
            Path filePath = javaFile.toPath();
            
            // Check if file is within project directory
            if (!filePath.startsWith(projectPath)) {
                // File is outside project directory - handle specially
                return handleExternalJavaFile(projectRoot, javaFile);
            }
            
            Path relativePath = projectPath.relativize(filePath);
            
            // Convert to package format
            String className = relativePath.toString()
                .replace(File.separator, ".")
                .replace(".java", "");
            
            return className;
        } catch (IllegalArgumentException e) {
            // Handle files on different drives (Windows) or other issues
            return handleExternalJavaFile(projectRoot, javaFile);
        }
    }
    
    private String handleExternalJavaFile(File projectRoot, File javaFile) {
        // Show warning that file is outside project directory
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("File Location Warning");
        alert.setHeaderText("The selected Java file is outside the project directory");
        alert.setContentText("For proper execution, the main class file should be within the project directory. " +
                            "Would you like to copy it to your project directory?");
        
        ButtonType copyButton = new ButtonType("Copy to Project");
        ButtonType useAsIsButton = new ButtonType("Use As Is");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(copyButton, useAsIsButton, cancelButton);
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == copyButton) {
                // Copy file to project directory
                return copyFileToProject(projectRoot, javaFile);
            } else if (result.get() == useAsIsButton) {
                // Use file as is (outside project directory)
                return convertExternalFilePathToClassName(javaFile);
            }
        }
        return null;
    }
    
    private String copyFileToProject(File projectRoot, File javaFile) {
        // Create src directory if it doesn't exist
        File srcDir = new File(projectRoot, "src");
        if (!srcDir.exists()) {
            srcDir.mkdirs();
        }
        
        // Copy file to project
        File destFile = new File(srcDir, javaFile.getName());
        try {
            java.nio.file.Files.copy(javaFile.toPath(), destFile.toPath(), 
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            // Return new class name
            return "src." + javaFile.getName().replace(".java", "");
        } catch (Exception e) {
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Copy Failed");
            errorAlert.setHeaderText("Failed to copy file to project directory");
            errorAlert.setContentText(e.getMessage());
            errorAlert.showAndWait();
            return null;
        }
    }
    
    private String convertExternalFilePathToClassName(File javaFile) {
        // Simple conversion when file is outside project
        String fileName = javaFile.getName();
        if (fileName.endsWith(".java")) {
            return fileName.substring(0, fileName.length() - 5);
        }
        return fileName;
    }

    private void configureAndSaveProject(File location) {
        configureAndSaveProject(location, "com.example.Main");
    }

    private void configureAndSaveProject(File location, String suggestedMainClass) {
        TextInputDialog nameDialog = new TextInputDialog("My Project");
        nameDialog.setTitle("Project Setup");
        nameDialog.setHeaderText("Enter project name:");
        Optional<String> nameResult = nameDialog.showAndWait();
        
        TextInputDialog classDialog = new TextInputDialog(suggestedMainClass);
        classDialog.setTitle("Project Setup");
        classDialog.setHeaderText("Enter fully-qualified main class name:");
        Optional<String> classResult = classDialog.showAndWait();
        
        if (nameResult.isPresent() && classResult.isPresent()) {
            ProjectManager.saveProject(
                nameResult.get(),
                location.getAbsolutePath(),
                classResult.get()
            );
            
            loadUserProjects();
            loadModules();
        }
    }

    private String detectMainClass(File projectDir) {
        // Look for a main class file in common locations
        File[] candidates = {
            new File(projectDir, "src/main/java/Main.java"),
            new File(projectDir, "src/Main.java"),
            new File(projectDir, "app/Main.java"),
            new File(projectDir, "Main.java")
        };
        
        for (File candidate : candidates) {
            if (candidate.exists() && candidate.isFile()) {
                return convertFilePathToClassName(projectDir, candidate);
            }
        }
        
        // Check for Maven/Gradle projects
        if (new File(projectDir, "pom.xml").exists()) {
            return "com.example.Main";
        }
        if (new File(projectDir, "build.gradle").exists()) {
            return "com.example.Main";
        }
        
        return "Main";
    }

    private void showErrorAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @FXML
    @SuppressWarnings("unused")
    private void goNext() {
        currentPage++;
        loadModules();
    }

    @FXML
    @SuppressWarnings("unused")
    private void goBack() {
        if (currentPage > 0) currentPage--;
        loadModules();
    }
    
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
}