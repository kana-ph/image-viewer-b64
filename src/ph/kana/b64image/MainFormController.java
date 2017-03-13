package ph.kana.b64image;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;

import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Pane;
import javafx.stage.Window;
import ph.kana.b64image.dialog.DialogService;
import ph.kana.b64image.file.FileOperationException;
import ph.kana.b64image.file.FileService;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Base64;
import java.util.Optional;

public class MainFormController {

	private FileService fileService = FileService.getInstance();
	private DialogService dialogService = DialogService.getInstance();
	private static final long FILE_SIZE_30MB_LIMIT = 30_000_000;

	@FXML private TextArea base64TextArea;

	@FXML private Pane rootPane;

	@FXML private ProgressIndicator updateTextProgress;
	@FXML private Menu fileMenu;
	@FXML private Menu editMenu;
	@FXML private Button decodeButton;

	@FXML
	public void openBase64Image() {
		parseFileData()
			.ifPresent(this::openToDesktop);
	}

	@FXML
	public void openFileAsBase64() {
		dialogService
			.showOpenRegularFileDialog(getWindow())
			.ifPresent(this::convertToBase64);
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
			copyText(selectedText);
		}
	}

	@FXML
	public void copyAllText() {
		copyText(base64TextArea.getText());
	}

	@FXML
	public void pasteText() {
		startTaskWithUiLock(base64TextArea::paste);
	}

	@FXML
	public void pasteAllText() {
		startTaskWithUiLock(() -> {
			base64TextArea.setText("");
			base64TextArea.paste();
		});
	}

	@FXML
	public void showAbout() {
		dialogService.showAboutDialog(getWindow());
	}

	private Window getWindow() {
		return rootPane
			.getScene()
			.getWindow();
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
			dialogService.showErrorDialog(getWindow(), e);
		}
	}

	private void convertToBase64(File file) {
		try {
			long fileSize = file.length();
			boolean fileTooLarge = (fileSize >= FILE_SIZE_30MB_LIMIT);

			if (fileTooLarge) {
				String message = String.format("Input file larger than 30 MB!\nGiven=%.2f MB", convertToMb(fileSize));
				throw new FileOperationException(message);
			} else {
				byte[] bytes = fileService.readBytes(file);
				String base64 = Base64
					.getEncoder()
					.encodeToString(bytes);
				startTaskWithUiLock(() -> base64TextArea.setText(base64));
			}
		} catch (FileOperationException e) {
			dialogService.showErrorDialog(getWindow(), e);
		}
	}

	private void copyText(String text) {
		ClipboardContent content = new ClipboardContent();
		content.putString(text);
		Clipboard
			.getSystemClipboard()
			.setContent(content);
	}

	private double convertToMb(long size) {
		return ((double) size) / 1_000_000;
	}

	private void startTaskWithUiLock(Runnable task) {
		Task updateTextTask = new Task<Void>() {
			@Override
			public Void call() {
				task.run();
				return null;
			}
		};
		bindLocks(updateTextTask.runningProperty());
		new Thread(updateTextTask, "update-text-thread")
			.start();
	}

	private void bindLocks(ReadOnlyBooleanProperty booleanProperty) {
		updateTextProgress
			.visibleProperty()
			.bind(booleanProperty);
		base64TextArea
			.disableProperty()
			.bind(booleanProperty);
		fileMenu
			.disableProperty()
			.bind(booleanProperty);
		editMenu
			.disableProperty()
			.bind(booleanProperty);
		decodeButton
			.disableProperty()
			.bind(booleanProperty);
	}
}
