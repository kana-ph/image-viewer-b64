package ph.kana.b64image;

import javafx.application.Platform;
import javafx.fxml.FXML;
import static javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Window;
import ph.kana.b64image.file.FileOperationException;
import ph.kana.b64image.file.FileService;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

public class MainFormController {

	private FileService fileService = FileService.getInstance();

	@FXML
	private TextArea base64TextArea;

	@FXML
	private Pane rootPane;

	@FXML
	public void openBase64Image() {
		parseFileData()
			.ifPresent(this::openToDesktop);
	}

	@FXML
	public void openFileAsBase64() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		Window window = rootPane
			.getScene()
			.getWindow();
		Optional<File> file = Optional
			.ofNullable(fileChooser.showOpenDialog(window));
		file.ifPresent(this::convertToBase64);
	}

	@FXML
	public void closeApp() {
		Platform.exit();
		System.exit(0);
	}

	@FXML
	public void copySelectedText() {
		String selectedText = base64TextArea.getSelectedText();
		if (selectedText.isEmpty()) {
			copyAllText();
		} else {
			base64TextArea.copy();
		}
	}

	@FXML
	public void copyAllText() {
		ClipboardContent content = new ClipboardContent();
		content.putString(base64TextArea.getText());
		Clipboard.getSystemClipboard().setContent(content);
	}

	@FXML
	public void pasteText() {
		base64TextArea.paste();
	}

	@FXML
	public void pasteAllText() {
		base64TextArea.setText("");
		base64TextArea.paste();
	}

	@FXML
	public void showAbout() {
		Dialog<ButtonType> dialog = new Dialog<>();
		dialog.setTitle("About ImageViewer-B64");
		dialog.setContentText("I'm open-source!\nCreated by @_kana0011");
		dialog.initModality(Modality.APPLICATION_MODAL);

		ButtonType githubButton = new ButtonType("View Github", ButtonData.OK_DONE);
		List<ButtonType> buttons = dialog
			.getDialogPane()
			.getButtonTypes();
		buttons.add(githubButton);
		buttons.add(new ButtonType("Close", ButtonData.CANCEL_CLOSE));

		dialog
			.showAndWait()
			.filter(githubButton::equals)
			.ifPresent(b -> openGithub());
	}

	private Optional<InputStream> parseFileData() {
		String base64 = base64TextArea
			.getText()
			.replaceAll("[\\s]+", "");
		if (!base64.isEmpty()) {
			byte[] bytes = Base64
				.getDecoder()
				.decode(base64);
			return Optional
				.of(new ByteArrayInputStream(bytes));
		} else {
			return Optional.empty();
		}
	}

	private void openToDesktop(InputStream inputStream) {
		try {
			File tempFile = fileService.createTempFile(inputStream);
			fileService.openToDesktop(tempFile);
		} catch (FileOperationException e) {
			e.printStackTrace(System.err);
		}
	}

	private void convertToBase64(File file) {
		try {
			byte[] bytes = fileService.readBytes(file);
			String base64 = Base64
				.getEncoder()
				.encodeToString(bytes);
			base64TextArea.setText(base64);
		} catch (FileOperationException e) {
			e.printStackTrace(System.err);
		}
	}

	private void openGithub() {
		try {
			URI uri = new URI("https://github.com/kana0011/image-viewer-b64");
			fileService.openToDesktop(uri);
		} catch (URISyntaxException e) {
			e.printStackTrace(System.err);
		}
	}
}
