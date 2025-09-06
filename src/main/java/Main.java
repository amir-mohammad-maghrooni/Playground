import javafx.application.Application;

/**
 * Entry point that delegates to HubApp to keep a clean separation
 * between the bootstrap class (for shade manifest) and the actual App.
 */
public class Main {
    public static void main(String[] args) {
        Application.launch(HubApp.class, args);
    }
}