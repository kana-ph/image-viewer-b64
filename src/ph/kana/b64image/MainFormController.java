package ph.kana.b64image;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import ph.kana.b64image.file.FileOperationException;
import ph.kana.b64image.file.FileService;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Base64;
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
}
