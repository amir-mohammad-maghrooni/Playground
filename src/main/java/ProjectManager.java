import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * ProjectManager (singleton) is responsible for persistence and data access.
 * It serializes the list of modules to a JSON config file and loads them
 * back on next run.
 */
public class ProjectManager {
    private static final ProjectManager INSTANCE = new ProjectManager();

    public static ProjectManager getInstance() {
        return INSTANCE;
    }

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Config dir: user home/.playground ; file: config.json
    private final Path configDir = Path.of(System.getProperty("user.home"), ".playground");
    private final Path configFile = configDir.resolve("config.json");

    private ProjectManager() {
    }

    /** Load modules from config; returns an empty list if none exist. */
    public List<ProjectModule> loadModules() {
        try {
            if (!Files.exists(configFile))
                return new ArrayList<>();
            String json = Files.readString(configFile, StandardCharsets.UTF_8);
            Type listType = new TypeToken<List<StoredModule>>() {
            }.getType();
            List<StoredModule> stored = gson.fromJson(json, listType);
            List<ProjectModule> result = new ArrayList<>();
            for (StoredModule sm : stored) {
                result.add(new ExternalProjectModule(sm.name, sm.description, sm.path, sm.type));
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /** Save modules to config, creating directories as needed. */
    public void saveModules(List<ProjectModule> modules) {
        try {
            if (!Files.exists(configDir))
                Files.createDirectories(configDir);
            List<StoredModule> stored = new ArrayList<>();
            for (ProjectModule pm : modules) {
                stored.add(new StoredModule(pm.getName(), pm.getDescription(), pm.getPath(), pm.getType()));
            }
            String json = gson.toJson(stored);
            Files.writeString(configFile, json, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Plain DTO used for JSON persistence */
    static class StoredModule {
        String name;
        String description;
        String path;
        ProjectType type;

        StoredModule(String n, String d, String p, ProjectType t) {
            this.name = n;
            this.description = d;
            this.path = p;
            this.type = t;
        }
    }
}