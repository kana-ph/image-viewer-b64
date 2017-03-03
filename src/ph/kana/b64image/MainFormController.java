package ph.kana.b64image;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
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
	public void openBase64Image() {
		parseFileData()
			.ifPresent(this::openToDesktop);
	}

	private Optional<InputStream> parseFileData() {
		String base64 = base64TextArea
			.getText()
			.replaceAll("\n", "");
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
}
