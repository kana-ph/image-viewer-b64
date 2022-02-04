module ph.kana.b64image {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.xml;
    requires org.apache.tika.core;

    opens ph.kana.b64image to javafx.fxml;
    opens ph.kana.b64image.dialog to javafx.fxml;

    exports ph.kana.b64image;
    exports ph.kana.b64image.dialog;
}
