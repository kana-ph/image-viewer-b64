package ph.kana.b64image.file;


public enum FileSizeLimit {
	INPUT(30_000_000),
	BASE64(100_000_000);

	final long value;

	FileSizeLimit(long value) {
		this.value = value;
	}

	public long getValue() {
		return value;
	}
}
