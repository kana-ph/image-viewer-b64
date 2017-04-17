package ph.kana.b64image.file;

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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collector;
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

	public Map<String, String> identifyFile(InputStream inputStream) throws FileOperationException {
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

	private File writeToFile(final File file, InputStream inputStream) throws IOException {
		try (OutputStream outputStream = new FileOutputStream(file)) {
			for (int i = inputStream.read(); i != -1; i = inputStream.read()) {
				outputStream.write(i);
			}
			outputStream.flush();
		}
		return file;
	}

	private Map<String, String> buildFileInfo(Metadata metadata) {
		String[] names = metadata.names();
		Arrays.sort(names);
		return Arrays.stream(names)
			.collect(metadataMapper(metadata));
	}

	private Collector<String, ?, Map<String, String>> metadataMapper(Metadata metadata) {
		Function<String, String> keyMapper = String::toString;
		Function<String, String> valueMapper = metadata::get;
		BinaryOperator<String> mergeFunction = (a, b) -> b;
		return Collectors.toMap(keyMapper, valueMapper, mergeFunction, LinkedHashMap::new);
	}
}
