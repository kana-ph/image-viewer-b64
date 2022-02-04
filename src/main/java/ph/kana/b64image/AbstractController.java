package ph.kana.b64image;


import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

abstract class AbstractController {
	@FXML protected Pane rootPane;

	protected Stage getWindow() {
		return (Stage) rootPane
			.getScene()
			.getWindow();
	}
}
