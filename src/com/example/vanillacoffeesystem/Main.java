package com.example.vanillacoffeesystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader fxmlLoader = new FXMLLoader(
                Main.class.getResource(ViewPaths.fxml("home-view.fxml"))
        );

        Scene scene = SceneHelper.create(fxmlLoader.load());

        stage.setTitle("Vanilla Coffee");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}