package ph.kana.b64image;

import javafx.fxml.FXML;
import javafx.stage.Stage;
import ph.kana.b64image.dialog.DialogService;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class AboutDialogController extends AbstractController {

	private final DialogService dialogService = DialogService.getInstance();

	@FXML
	public void openLink() {
		try {
			openGithub();
		} catch (URISyntaxException e) {
			dialogService.showErrorDialog(getWindow(), e);
		}
	}

	public void closeDialog() {
		Stage window = getWindow();
		window.close();
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
