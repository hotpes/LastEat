package com.lasteat.upload;

import java.util.Arrays;
import java.util.Optional;

// 허용하는 이미지 형식. 파일 이름·Content-Type은 믿지 않고 파일 앞부분(매직 바이트)으로 판단한다.
public enum ImageType {
	JPEG("jpg"),
	PNG("png"),
	WEBP("webp");

	// 판별에 필요한 앞부분 길이 (WEBP: RIFF....WEBP)
	public static final int HEADER_LENGTH = 12;

	private static final byte[] JPEG_MAGIC = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};

	private static final byte[] PNG_MAGIC = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};

	private static final byte[] RIFF = {'R', 'I', 'F', 'F'};

	private static final byte[] WEBP_MAGIC = {'W', 'E', 'B', 'P'};

	private static final int WEBP_MAGIC_OFFSET = 8;

	private final String extension;

	ImageType(String extension) {
		this.extension = extension;
	}

	public String extension() {
		return extension;
	}

	public static Optional<ImageType> detect(byte[] header) {
		if (startsWith(header, 0, JPEG_MAGIC)) {
			return Optional.of(JPEG);
		}
		if (startsWith(header, 0, PNG_MAGIC)) {
			return Optional.of(PNG);
		}
		if (startsWith(header, 0, RIFF) && startsWith(header, WEBP_MAGIC_OFFSET, WEBP_MAGIC)) {
			return Optional.of(WEBP);
		}
		return Optional.empty();
	}

	private static boolean startsWith(byte[] data, int offset, byte[] magic) {
		return data.length >= offset + magic.length
				&& Arrays.equals(data, offset, offset + magic.length, magic, 0, magic.length);
	}

}
