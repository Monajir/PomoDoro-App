package lab.visual.pomodoroapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class MainApp extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
        try{
            Parent root = fxmlLoader.load();
            scene = new Scene(root);

            // Default to light theme
            URL cssURL = MainApp.class.getResource("/css/light-theme.css");
            if (cssURL == null) {
                throw new IllegalArgumentException("Theme CSS not found: " + "/css/light-theme.css");
            }
            scene.getStylesheets().add(cssURL.toExternalForm());
            stage.setScene(scene);
            stage.setTitle("220041132");
            stage.show();
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public static void setTheme(String theme) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(MainApp.class.getResource("/css/" + theme + "-theme.css").toExternalForm());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
