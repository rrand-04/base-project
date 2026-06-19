package vanillacoffeesystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

// Application entry point. Loads the home screen on startup.
public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(
                Main.class.getResource(ViewPaths.fxml("home-view.fxml"))
        );
        Parent root = fxmlLoader.load();
        stage.setTitle("Vanilla Coffee");
        stage.setScene(SceneHelper.create(root));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
