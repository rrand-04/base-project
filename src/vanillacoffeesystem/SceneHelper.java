package vanillacoffeesystem;

import javafx.scene.Parent;
import javafx.scene.Scene;

// Shared scene size used when navigating between screens.
public final class SceneHelper {

    private SceneHelper() {
    }

    public static Scene create(Parent root) {
        return new Scene(root, 1100, 720);
    }
}
