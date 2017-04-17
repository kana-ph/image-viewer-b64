package ph.kana.b64image.dialog;


import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.FileChooser;
import static javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Window;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class DialogService {
	private static DialogService instance = new DialogService();
	private static final String DIALOG_TITLE = "ImageViewer-B64";

	public static DialogService getInstance() {
		return instance;
	}

	private DialogService() { }

	public Optional<File> showOpenRegularFileDialog(Window parent) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open File to Convert");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

		return Optional
			.ofNullable(fileChooser.showOpenDialog(parent));
	}

	public Optional<File> showOpenBase64FileDialog(Window parent) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Base64 Text File");
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

		ExtensionFilter textFileFilter = new ExtensionFilter("Base64 Text Files (*.txt, *.b64, *.base64)", "*.txt", "*.b64", "*.base64");
		fileChooser
			.getExtensionFilters()
			.add(textFileFilter);
		fileChooser.setSelectedExtensionFilter(textFileFilter);

		return Optional
			.ofNullable(fileChooser.showOpenDialog(parent));
	}

	public void showAboutDialog(Window parent) {
		Dialog<ButtonType> dialog = new Dialog<>();
		dialog.initOwner(parent);
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.setTitle("About: " + DIALOG_TITLE);
		dialog.setContentText("I'm open-source!\nCreated by @_kana0011");

		ButtonType githubButton = new ButtonType("View at Github", ButtonBar.ButtonData.OK_DONE);
		List<ButtonType> buttons = dialog
			.getDialogPane()
			.getButtonTypes();
		buttons.add(githubButton);
		buttons.add(new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE));

		dialog
			.showAndWait()
			.filter(githubButton::equals)
			.ifPresent(b -> {
				try {
					openGithub();
				} catch (URISyntaxException e) {
					showErrorDialog(parent, e);
				}
			});
	}

	public void showErrorDialog(Window parent, Exception e) {
		e.printStackTrace(System.err);

		Alert warningDialog = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.CLOSE);
		warningDialog.initOwner(parent);
		warningDialog.initModality(Modality.APPLICATION_MODAL);
		warningDialog.setTitle(DIALOG_TITLE + " - Error!");
		warningDialog
			.getDialogPane()
			.getButtonTypes();
		warningDialog.showAndWait();
	}

	public void showFileInfo(Window parent, Map<String, String> fileInfo) {
		Dialog<ButtonType> dialog = new Dialog<>();
		dialog.initOwner(parent);
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.setTitle(DIALOG_TITLE + " - File Identification");

		String dialogContent = formatFileInfo(fileInfo);
		dialog.setContentText(dialogContent);
		List<ButtonType> buttons = dialog
			.getDialogPane()
			.getButtonTypes();
		buttons.add(new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE));

		dialog.showAndWait();
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

	private String formatFileInfo(Map<String, String> fileInfo) {
		StringBuilder message = new StringBuilder();
		fileInfo
			.entrySet()
			.stream()
			.map(e -> String.format("%s: %s\n", e.getKey(), e.getValue()))
			.forEach(message::append);
		return message.toString();
	}
}
