package com.example.vanillacoffeesystem;

import javafx.scene.Parent;
import javafx.scene.Scene;

public final class SceneHelper {

    private static final String STYLESHEET =
            SceneHelper.class.getResource("/com/example/vanillacoffeesystem/styles/app.css")
                    .toExternalForm();

    private SceneHelper() {
    }

    public static Scene create(Parent root) {
        Scene scene = new Scene(root, 1100, 720);
        scene.getStylesheets().add(STYLESHEET);
        return scene;
    }

    public static void applyTheme(Scene scene) {
        if (!scene.getStylesheets().contains(STYLESHEET)) {
            scene.getStylesheets().add(STYLESHEET);
        }
    }
}
