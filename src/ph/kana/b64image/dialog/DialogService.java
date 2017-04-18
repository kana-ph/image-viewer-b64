package ph.kana.b64image.dialog;


import javafx.event.*;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.*;

import static javafx.stage.FileChooser.ExtensionFilter;

import javafx.stage.Window;
import ph.kana.b64image.MetadataDialogController;
import ph.kana.b64image.file.FileMetadata;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

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
		String fxmlFile = "about-dialog";
		String title = "About: " + DIALOG_TITLE;

		Stage aboutDialog = createDialog(fxmlFile, title, parent, c -> {});
		aboutDialog.showAndWait();
	}

	public void showErrorDialog(Window parent, Exception e) {
		e.printStackTrace(System.err);

		Alert warningDialog = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.CLOSE);
		warningDialog.initOwner(parent);
		warningDialog.initModality(Modality.APPLICATION_MODAL);
		warningDialog.setTitle(DIALOG_TITLE + " - Error!");
		warningDialog.showAndWait();
	}

	public void showFileInfo(Window parent, List<FileMetadata> fileInfo) {
		String fxmlFile = "metadata-dialog";
		String title = DIALOG_TITLE + " - File Identification";
		Consumer<MetadataDialogController> initialize = controller -> controller.setMetadata(fileInfo);

		Stage metadataDialog = createDialog(fxmlFile, title, parent, initialize);
		metadataDialog.showAndWait();
	}

	public boolean promptTextFileDrop(Window parent) {
		Dialog<ButtonType> promptDialog = new Dialog<>();
		promptDialog.initOwner(parent);
		promptDialog.initModality(Modality.APPLICATION_MODAL);
		promptDialog.initStyle(StageStyle.UNIFIED);
		promptDialog.setTitle(DIALOG_TITLE + " - File Dropped");
		promptDialog.setContentText("Dropped file is a valid Base64 Text File.\nWhat to do?");

		List<ButtonType> buttons = promptDialog
			.getDialogPane()
			.getButtonTypes();
		ButtonType plainTextButton = new ButtonType("Open as Base64", ButtonData.LEFT);
		buttons.add(plainTextButton);
		ButtonType convertButton = new ButtonType("Convert Text File to Base64", ButtonData.RIGHT);
		buttons.add(convertButton);

		ButtonType clicked = promptDialog
			.showAndWait()
			.orElse(null);

		return plainTextButton.equals(clicked);
	}

	private <C> Stage createDialog(String fxmlFile, String title, Window parent, Consumer<C> controllerAction) {
		String path = String.format("/ph/kana/b64image/%s.fxml", fxmlFile);
		FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
		try {
			Parent root = loader.load();

			Stage dialog = new Stage();
			dialog.initStyle(StageStyle.UNIFIED);
			dialog.initModality(Modality.APPLICATION_MODAL);
			dialog.initOwner(parent);

			dialog.setTitle(title);
			dialog.setScene(new Scene(root));
			dialog.sizeToScene();
			dialog.setResizable(false);

			C dialogController = loader.getController();
			controllerAction.accept(dialogController);

			return dialog;
		} catch (IOException e) {
			showErrorDialog(parent, e);
		}
		return null;
	}
}
