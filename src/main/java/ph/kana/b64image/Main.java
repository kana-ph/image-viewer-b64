package ph.kana.b64image;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ph.kana.b64image.file.HostServicesHolder;

public class Main extends Application {

    private static final double APP_WIDTH = 650.0;
    private static final double APP_HEIGHT = 600.0;

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main-form.fxml"));
        Parent root = loader.load();
        setUserAgentStylesheet(STYLESHEET_MODENA);
        primaryStage.setTitle("kana-ph/image-viewer-b64");
        primaryStage.setScene(new Scene(root, APP_WIDTH, APP_HEIGHT));
        primaryStage.setMinWidth(APP_WIDTH);
        primaryStage.setMinHeight(APP_HEIGHT);
        HostServicesHolder.setHostServices(getHostServices());
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
