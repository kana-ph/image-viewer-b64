package ph.kana.b64image.file;

import org.apache.tika.Tika;

import java.awt.*;
import java.io.*;
import java.util.Map;
import java.util.TreeMap;

public class FileService {
	private static FileService instance = new FileService();

	private static final Map<String, String> SUPPORTED_TYPES = new TreeMap<>();
	static {
		SUPPORTED_TYPES.put("image/jpeg", "jpg");
		SUPPORTED_TYPES.put("image/png", "png");
		SUPPORTED_TYPES.put("image/bmp", "bmp");
		SUPPORTED_TYPES.put("image/gif", "gif");
		SUPPORTED_TYPES.put("image/tiff", "tiff");
		SUPPORTED_TYPES.put("application/pdf", "pdf");
		SUPPORTED_TYPES.put("application/rtf", "rtf");
		SUPPORTED_TYPES.put("text/plain", "txt");
	}

	public static FileService getInstance() {
		return instance;
	}

	private FileService() {	}

	public File createTempFile(InputStream inputStream) throws FileOperationException {
		Tika tika = new Tika();
		try {
			String contentType = tika.detect(inputStream);
			validateContentType(contentType);

			String extension = "." + SUPPORTED_TYPES.get(contentType);
			File file = File.createTempFile("ivb64-", extension);
			file.deleteOnExit();

			return writeToFile(file, inputStream);
		} catch (IOException e) {
			throw new FileOperationException("Failed to parse base 64", e);
		}
	}

	public void openToDesktop(File file) {
		new Thread(() -> {
			try {
				Desktop desktop = Desktop.getDesktop();
				desktop.open(file);
			} catch (IOException e) { e.printStackTrace(System.err); }
		}, "file-launcher-thread")
			.start();
	}

	private void validateContentType(String contentType) throws FileOperationException {
		if (!SUPPORTED_TYPES.containsKey(contentType)) {
			throw new FileOperationException("Base 64 parsed to an unsupported file: " + contentType);
		}
	}

	private File writeToFile(final File file, InputStream inputStream) throws IOException {
		try (OutputStream outputStream = new FileOutputStream(file)) {
			for (int i = inputStream.read(); i != -1; i = inputStream.read()) {
				outputStream.write(i);
			}
			outputStream.flush();
		}
		return file;
	}
}
