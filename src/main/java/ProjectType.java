import java.nio.file.Path;

/**
 * Simple enum for supported project types with a helper to infer from Path.
 */
public enum ProjectType {
    EXE, JAVA, BAT, HTML, JAR, UNKNOWN;

    public static ProjectType fromPath(Path p) {
        String name = p.getFileName().toString().toLowerCase();
        if (name.endsWith(".exe"))
            return EXE;
        if (name.endsWith(".java"))
            return JAVA;
        if (name.endsWith(".bat"))
            return BAT;
        if (name.endsWith(".html") || name.endsWith(".htm"))
            return HTML;
        if (name.endsWith(".jar"))
            return JAR;
        return UNKNOWN;
    }
}