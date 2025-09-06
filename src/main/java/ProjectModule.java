/**
 * Base model for a "module" in the Playground. This could be an external
 * executable/script, a JAR, a Java source file, or an HTML page.
 *
 * Follows OOP principles by encapsulating data and exposing behavior
 * through the abstract launchNewWindow() method.
 */
public abstract class ProjectModule {
    private String name;
    private String description;
    private String path; // absolute path
    private ProjectType type;

    public ProjectModule(String name, String description, String path, ProjectType type) {
        this.name = name;
        this.description = description;
        this.path = path;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPath() {
        return path;
    }

    public ProjectType getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setType(ProjectType type) {
        this.type = type;
    }

    /**
     * Implementations create a new Stage (window) and either embed a UI
     * (e.g., WebView for HTML) or spawn an external process while displaying
     * process output. Multiple instances are allowed by design.
     */
    public abstract void launchNewWindow();
}