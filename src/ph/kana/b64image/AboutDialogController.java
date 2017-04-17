package ph.kana.b64image;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Window;
import ph.kana.b64image.dialog.DialogService;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class AboutDialogController {

	private final DialogService dialogService = DialogService.getInstance();

	@FXML Pane rootPane;

	@FXML
	public void openLink() {
		try {
			openGithub();
		} catch (URISyntaxException e) {
			dialogService.showErrorDialog(getWindow(), e);
		}
	}

	public void closeDialog() {
		Stage window = (Stage) getWindow();
		window.close();
	}

	private Window getWindow() {
		return rootPane
			.getScene()
			.getWindow();
	}

	private void openGithub() throws URISyntaxException {
		URI uri = new URI("https://github.com/kana0011/image-viewer-b64");
		openInBrowser(uri);
	}

	private void openInBrowser(URI uri) {
		new Thread(() -> {
			try {
				Desktop desktop = Desktop.getDesktop();
				desktop.browse(uri);
			} catch (IOException e) { e.printStackTrace(System.err); }
		}, "web-browser-launcher-thread")
			.start();
	}
}
