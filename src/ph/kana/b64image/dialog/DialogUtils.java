package ph.kana.b64image.dialog;


import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.Optional;

public final class DialogUtils {

	private DialogUtils() { }

	public static Optional<File> openFile(Window parent, String title) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(title);
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

		return Optional
			.ofNullable(fileChooser.showOpenDialog(parent));
	}
}
