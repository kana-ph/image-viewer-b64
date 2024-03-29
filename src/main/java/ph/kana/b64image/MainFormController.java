package ph.kana.b64image;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;

import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.util.Duration;
import ph.kana.b64image.dialog.DialogService;
import ph.kana.b64image.dialog.DndInitializer;
import ph.kana.b64image.file.FileMetadata;
import ph.kana.b64image.file.FileSizeLimit;
import ph.kana.b64image.file.FileOperationException;
import ph.kana.b64image.file.FileService;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

public class MainFormController extends AbstractController {

	private FileService fileService = FileService.getInstance();
	private DialogService dialogService = DialogService.getInstance();

	@FXML private TextArea base64TextArea;

	@FXML private ProgressIndicator updateTextProgress;
	@FXML private Label messageLabel;
	@FXML private Menu fileMenu;
	@FXML private Menu editMenu;
	@FXML private Button decodeButton;

	public void initialize() {
		DndInitializer dndInitializer = DndInitializer
			.withFileAction(this::openDroppedFile);
		rootPane.setOnDragOver(dndInitializer::dragOverEvent);
		rootPane.setOnDragDropped(dndInitializer::dragDroppedEvent);
	}

	@FXML
	public void openBase64Image() {
		showMessage("Opening Base64 file to desktop...");
		parseFileData()
			.ifPresent(this::openToDesktop);
	}

	@FXML
	public void identify() {
		showMessage("Identifying Base64 file...");
		parseFileData()
			.ifPresent(this::showFileInfo);
	}

	@FXML
	public void openFileAsBase64() {
		dialogService
			.showOpenRegularFileDialog(getWindow())
			.ifPresent(this::convertToBase64);
	}

	@FXML
	public void openBase64File() {
		dialogService
			.showOpenBase64FileDialog(getWindow())
			.ifPresent(this::showBase64File);
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
		showMessage("All text copied to clipboard!");
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
		showMessage("All text replaced from clipboard!");
	}

	@FXML
	public void showAbout() {
		dialogService.showAboutDialog(getWindow());
	}

	private Optional<InputStream> parseFileData() {
		String base64 = base64TextArea
			.getText()
			.replaceAll("[\\s]+", "");
		if (!base64.isEmpty()) {
			try {
				byte[] bytes = fileService.decodeBase64(base64);
				return Optional
					.of(new ByteArrayInputStream(bytes));
			} catch (FileOperationException e) {
				dialogService.showErrorDialog(getWindow(), e);
				return Optional.empty();
			}
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
			long sizeLimit = FileSizeLimit.INPUT.getValue();
			boolean fileTooLarge = (fileSize >= sizeLimit);

			if (fileTooLarge) {
				String message = String.format("Input file larger than %.2f MB!\nGiven=%.2f MB",
					convertToMb(sizeLimit), convertToMb(fileSize));
				throw new FileOperationException(message);
			} else {
				byte[] bytes = fileService.readBytes(file);
				String base64 = Base64
					.getEncoder()
					.encodeToString(bytes);
				startTaskWithUiLock(() -> base64TextArea.setText(base64));
				showMessage(String.format("Converted to Base64: '%s'", file.getAbsolutePath()));
			}
		} catch (FileOperationException e) {
			dialogService.showErrorDialog(getWindow(), e);
		}
	}

	private void showBase64File(File file) {
		try {
			long fileSize = file.length();
			long sizeLimit = FileSizeLimit.BASE64.getValue();
			boolean fileTooLarge = (fileSize >= sizeLimit);

			if (fileTooLarge) {
				String message = String.format("Base64 file larger than %.2f MB!\nGiven=%.2f MB",
					convertToMb(sizeLimit), convertToMb(fileSize));
				throw new FileOperationException(message);
			} else {
				byte[] bytes = fileService.readBytes(file);
				String base64 = new String(bytes)
					.replaceAll("[\\s]+", "");
				startTaskWithUiLock(() -> base64TextArea.setText(base64));
				showMessage(String.format("Opened Base64 text file: '%s'", file.getAbsolutePath()));
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
		Task<Void> updateTextTask = new Task<>() {
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

	private void openDroppedFile(File file) {
		try {
			boolean validBase64 = fileService.validBase64File(file);
			if (validBase64 && dialogService.promptTextFileDrop(getWindow())) {
				showBase64File(file);
			} else {
				convertToBase64(file);
			}
		} catch (FileOperationException e) {
			dialogService.showErrorDialog(getWindow(), e);
		}
	}

	private void showFileInfo(InputStream inputStream) {
		try {
			List<FileMetadata> fileInfo = fileService.identifyFile(inputStream);
			dialogService.showFileInfo(getWindow(), fileInfo);
		} catch (FileOperationException e) {
			dialogService.showErrorDialog(getWindow(), e);
		}
	}

	private void showMessage(String message) {
		messageLabel.setText(message);
		messageLabel.setOpacity(1.0);

		FadeTransition fadeTransition = new FadeTransition(Duration.millis(200.0), messageLabel);
		fadeTransition.setDelay(Duration.millis(3000.0));
		fadeTransition.setFromValue(1.0);
		fadeTransition.setToValue(0.0);
		fadeTransition.setCycleCount(1);
		fadeTransition.setAutoReverse(false);
		fadeTransition.play();
	}
}
