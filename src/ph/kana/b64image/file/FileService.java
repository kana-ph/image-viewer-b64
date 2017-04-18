package ph.kana.b64image.file;

import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FileService {
	private static FileService instance = new FileService();

	public static FileService getInstance() {
		return instance;
	}

	private FileService() {	}

	public File createTempFile(InputStream inputStream) throws FileOperationException {
		try {
			MimeType mimeType = determineContentType(inputStream);
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
		String base64Type = MimeTypes.PLAIN_TEXT;
		try {
			InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
			MimeType mimeType = determineContentType(inputStream);

			if (base64Type.equals(mimeType.getName())) {
				Tika tika = new Tika();
				String fileContent = tika.parseToString(inputStream, new Metadata());

				return validBase64(fileContent.trim());
			}
			return false;
		} catch (IOException | TikaException e) {
			throw new FileOperationException("Cannot read file: " + file.getAbsolutePath(), e);
		}
	}

	public List<FileMetadata> identifyFile(InputStream inputStream) throws FileOperationException {
		BodyContentHandler handler = new BodyContentHandler((int) FileSizeLimit.INPUT.getValue());
		AutoDetectParser parser = new AutoDetectParser();
		Metadata metadata = new Metadata();

		try {
			parser.parse(inputStream, handler, metadata);
			return buildFileInfo(metadata);
		} catch (IOException | SAXException | TikaException e) {
			throw new FileOperationException("Failed to identify Base64.", e);
		}
	}

	public byte[] decodeBase64(String base64) throws FileOperationException {
		if (validBase64(base64)) {
			return Base64
				.getDecoder()
				.decode(base64);
		} else {
			throw new FileOperationException("Invalid Base64!");
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

	private List<FileMetadata> buildFileInfo(Metadata metadata) {
		String[] names = metadata.names();
		Arrays.sort(names);
		return Arrays.stream(names)
			.map(name -> {
				FileMetadata fileMetadata = new FileMetadata();
				fileMetadata.setKey(name);
				fileMetadata.setValue(metadata.get(name));
				return  fileMetadata;
			})
			.collect(Collectors.toList());
	}

	private MimeType determineContentType(InputStream inputStream) throws IOException, MimeTypeException {
		MimeTypes mimeRepository = TikaConfig
			.getDefaultConfig()
			.getMimeRepository();

		MediaType mediaType = mimeRepository
			.detect(inputStream, new Metadata());
		return mimeRepository
			.forName(mediaType.toString());
	}

	private boolean validBase64(String base64) {
		Pattern base64Pattern = Pattern
			.compile("^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$");
		return base64Pattern
			.matcher(base64)
			.matches();
	}
}
