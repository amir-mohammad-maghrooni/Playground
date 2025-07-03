package Hub;

import utils.ProjectModule;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ProjectManager {
    private static final String CONFIG_FILE = "user_projects.config";

    public static void saveProject(String name, String path, String mainClass) {
        Properties props = new Properties();
        
        // Load existing config
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            props.load(input);
        } catch (IOException ignored) {}
        
        String key = "project." + System.currentTimeMillis();
        props.setProperty(key + ".name", name);
        props.setProperty(key + ".path", path);
        props.setProperty(key + ".main", mainClass);
        
        // Add type marker
        File f = new File(path);
        props.setProperty(key + ".type", f.isDirectory() ? "DIR" : "JAR");
        
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            props.store(output, "User Added Projects");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<ProjectModule> loadProjects() {
        List<ProjectModule> projects = new ArrayList<>();
        Properties props = new Properties();
        
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            props.load(input);
            
            for (String key : props.stringPropertyNames()) {
                if (key.startsWith("project.") && key.endsWith(".name")) {
                    String baseKey = key.substring(0, key.lastIndexOf("."));
                    String name = props.getProperty(key);
                    String path = props.getProperty(baseKey + ".path");
                    String mainClass = props.getProperty(baseKey + ".main");
                    
                    projects.add(new ExternalProjectModule(name, new File(path), mainClass));
                }
            }
        } catch (IOException e) {
            // Config file doesn't exist
        }
        return projects;
    }

    /**
     * Removes a project from the configuration file
     * @param project The ProjectModule to remove
     */
    public static void removeProject(ProjectModule project) {
        Properties props = new Properties();
        
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            props.load(input);
            
            // Find the project key that matches the module
            String keyToRemove = null;
            for (String key : props.stringPropertyNames()) {
                if (key.startsWith("project.") && key.endsWith(".name")) {
                    String baseKey = key.substring(0, key.lastIndexOf("."));
                    String name = props.getProperty(key);
                    String path = props.getProperty(baseKey + ".path");
                    String mainClass = props.getProperty(baseKey + ".main");
                    
                    // Check if this matches the project we want to remove
                    if (name.equals(project.getModuleName()) && 
                        matchesProjectModule(project, path, mainClass)) {
                        keyToRemove = baseKey;
                        break;
                    }
                }
            }
            
            // Remove all properties for this project
            if (keyToRemove != null) {
                props.remove(keyToRemove + ".name");
                props.remove(keyToRemove + ".path");
                props.remove(keyToRemove + ".main");
                props.remove(keyToRemove + ".type");
                
                // Save the updated properties
                try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
                    props.store(output, "User Added Projects");
                } catch (IOException e) {
                    throw new RuntimeException("Failed to save updated configuration", e);
                }
            } else {
                throw new RuntimeException("Project not found in configuration");
            }
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration file", e);
        }
    }

    /**
     * Helper method to check if a ProjectModule matches the stored path and main class
     * Uses reflection to access private fields in ExternalProjectModule
     */
    private static boolean matchesProjectModule(ProjectModule project, String storedPath, String storedMainClass) {
        if (project instanceof ExternalProjectModule) {
            try {
                ExternalProjectModule extProject = (ExternalProjectModule) project;
                
                // Use reflection to access private fields
                java.lang.reflect.Field pathField = ExternalProjectModule.class.getDeclaredField("projectLocation");
                java.lang.reflect.Field mainClassField = ExternalProjectModule.class.getDeclaredField("mainClassName");
                
                pathField.setAccessible(true);
                mainClassField.setAccessible(true);
                
                File projectLocation = (File) pathField.get(extProject);
                String mainClassName = (String) mainClassField.get(extProject);
                
                // Compare both path and main class for accurate matching
                return projectLocation.getAbsolutePath().equals(storedPath) && 
                       mainClassName.equals(storedMainClass);
                       
            } catch (Exception e) {
                // If reflection fails, fall back to name-only matching
                System.err.println("Warning: Could not access ExternalProjectModule fields, using name-only matching");
                return true;
            }
        }
        return false;
    }
}