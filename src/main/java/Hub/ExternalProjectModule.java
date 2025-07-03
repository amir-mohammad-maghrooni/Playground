package Hub;

import utils.ProjectModule;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.IOException;

public class ExternalProjectModule implements ProjectModule {
    private final String moduleName;
    private final File projectLocation;
    private final String mainClassName;
    private final boolean isJar;

    public ExternalProjectModule(String name, File location, String mainClass) {
        this.moduleName = name;
        this.projectLocation = location;
        this.mainClassName = mainClass;
        this.isJar = location.isFile() && location.getName().toLowerCase().endsWith(".jar");
    }

    @Override
    public String getModuleName() {
        return moduleName;
    }

    @Override
    public void launch() {
        // For JavaFX applications, use separate process to avoid conflicts
        if (isJar) {
            launchJarInSeparateProcess();
        } else {
            launchProjectInSeparateProcess();
        }
    }

    private void launchJarInSeparateProcess() {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "java", "-jar", projectLocation.getAbsolutePath()
            );
            pb.inheritIO();
            Process process = pb.start();
            
            // Monitor process in background thread
            new Thread(() -> {
                try {
                    int exitCode = process.waitFor();
                    if (exitCode != 0) {
                        Platform.runLater(() -> showErrorAlert("Process exited with code: " + exitCode));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
            
        } catch (IOException e) {
            Platform.runLater(() -> showDetailedErrorAlert(e));
        }
    }

    private void launchProjectInSeparateProcess() {
        try {
            // Build classpath
            StringBuilder classpath = new StringBuilder();
            classpath.append(projectLocation.getAbsolutePath());
            
            // Add all JAR files in project directory
            List<File> jars = findProjectJars(projectLocation);
            for (File jar : jars) {
                classpath.append(File.pathSeparator).append(jar.getAbsolutePath());
            }
            
            // Add current classpath to ensure JavaFX modules are available
            String currentClasspath = System.getProperty("java.class.path");
            if (currentClasspath != null && !currentClasspath.isEmpty()) {
                classpath.append(File.pathSeparator).append(currentClasspath);
            }
            
            ProcessBuilder pb = new ProcessBuilder(
                "java", "-cp", classpath.toString(), mainClassName
            );
            pb.inheritIO();
            Process process = pb.start();
            
            // Monitor process in background thread
            new Thread(() -> {
                try {
                    int exitCode = process.waitFor();
                    if (exitCode != 0) {
                        Platform.runLater(() -> showErrorAlert("Process exited with code: " + exitCode));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
            
        } catch (IOException e) {
            Platform.runLater(() -> showDetailedErrorAlert(e));
        }
    }

    // Alternative method for non-JavaFX applications (if you want to run them in-process)
    private void launchInProcess() {
        new Thread(() -> {
            try {
                // Build classpath
                List<URL> urls = new ArrayList<>();
                if (isJar) {
                    urls.add(projectLocation.toURI().toURL());
                } else {
                    urls.add(projectLocation.toURI().toURL());
                    List<File> jars = findProjectJars(projectLocation);
                    for (File jar : jars) {
                        urls.add(jar.toURI().toURL());
                    }
                }
                
                // Create isolated classloader
                URLClassLoader childLoader = new URLClassLoader(
                    urls.toArray(new URL[0]),
                    ClassLoader.getSystemClassLoader()
                );
                
                // Load main class
                Class<?> mainClass = childLoader.loadClass(mainClassName);
                
                // Check if it's a JavaFX application
                if (isJavaFXApplication(mainClass)) {
                    Platform.runLater(() -> showErrorAlert("JavaFX applications must be launched in separate process"));
                    return;
                }
                
                // Launch non-JavaFX application
                Method mainMethod = mainClass.getDeclaredMethod("main", String[].class);
                mainMethod.invoke(null, (Object) new String[]{});
                
            } catch (Exception e) {
                Platform.runLater(() -> showDetailedErrorAlert(e));
            }
        }).start();
    }

    private boolean isJavaFXApplication(Class<?> mainClass) {
        try {
            Class<?> applicationClass = Class.forName("javafx.application.Application");
            return applicationClass.isAssignableFrom(mainClass);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Launch Error");
        alert.setHeaderText("Failed to launch " + moduleName);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showDetailedErrorAlert(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Launch Error");
        alert.setHeaderText("Failed to launch " + moduleName);
        
        // Create expandable exception content
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String exceptionText = sw.toString();
        
        Label label = new Label("The exception stacktrace was:");
        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        VBox content = new VBox(label, textArea);
        content.setSpacing(10);

        alert.getDialogPane().setContent(content);
        alert.setResizable(true);
        alert.showAndWait();
    }

    private List<File> findProjectJars(File directory) {
        List<File> jars = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files == null) return jars;
        
        for (File file : files) {
            if (file.isFile() && file.getName().toLowerCase().endsWith(".jar")) {
                jars.add(file);
            } else if (file.isDirectory()) {
                jars.addAll(findProjectJars(file));
            }
        }
        return jars;
    }
}