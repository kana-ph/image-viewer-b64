package ph.kana.b64image.file;

import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FileService {
	private static FileService instance = new FileService();

	public static FileService getInstance() {
		return instance;
	}

	private FileService() {	}

	public File createTempFile(InputStream inputStream) throws FileOperationException {
		MimeTypes mimeRepository = TikaConfig
			.getDefaultConfig()
			.getMimeRepository();
		try {
			MediaType mediaType = mimeRepository
				.detect(inputStream, new Metadata());
			MimeType mimeType = mimeRepository
				.forName(mediaType.toString());

			String extension = mimeType.getExtension();
			File file = File.createTempFile("ivb64-", extension);
			file.deleteOnExit();

			return writeToFile(file, inputStream);
		} catch (MimeTypeException | IOException e) {
			throw new FileOperationException("Failed to parse Base64", e);
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

	public byte[] readBytes(File file) throws FileOperationException {
		try {
			return Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			throw new FileOperationException("Unable to convert file: " + file.getAbsolutePath(), e);
		}
	}

	public boolean validBase64File(File file) throws FileOperationException {
		Pattern base64Pattern = Pattern
			.compile("^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$");
		try {
			String fileContent = Files
				.readAllLines(file.toPath())
				.stream()
				.collect(Collectors.joining());
			return base64Pattern
				.matcher(fileContent)
				.matches();
		} catch (IOException e) {
			throw new FileOperationException("Cannot read file: " + file.getAbsolutePath(), e);
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
