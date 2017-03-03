package ph.kana.b64image;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main-form.fxml"));
        Parent root = loader.load();
        setUserAgentStylesheet(STYLESHEET_MODENA);
        primaryStage.setTitle("kana0011/image-viewer-b64");
        primaryStage.setScene(new Scene(root, 650, 600));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
