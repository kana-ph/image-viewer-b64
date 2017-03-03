package ph.kana.b64image.file;

public class FileOperationException extends Exception {

	public FileOperationException(String message, Throwable cause) {
		super(message, cause);
	}

	public FileOperationException(String message) {
		super(message);
	}
}
