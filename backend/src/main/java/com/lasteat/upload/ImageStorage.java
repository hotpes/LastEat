package com.lasteat.upload;

import com.lasteat.global.exception.BusinessException;
import com.lasteat.global.exception.ErrorCode;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

// 업로드 이미지를 {dir}/{용도}/{yyyy}/{MM}/{UUID}.{확장자}로 저장하고 /uploads/... 경로를 돌려준다.
// 파일 이름은 서버가 만들므로 사용자가 보낸 이름은 경로에 들어가지 않는다.
@Component
@RequiredArgsConstructor
public class ImageStorage {

	public static final String URL_PREFIX = "/uploads/";

	public static final DataSize MAX_SIZE = DataSize.ofMegabytes(10);

	private final UploadProperties properties;

	private final Clock clock;

	public String store(MultipartFile file, ImagePurpose purpose) {
		if (file.isEmpty()) {
			throw new BusinessException(ErrorCode.INVALID_FILE);
		}
		// 운영에서는 multipart 설정이 먼저 막는다. 설정이 바뀌어도 넘지 않게 한 번 더 확인
		if (file.getSize() > MAX_SIZE.toBytes()) {
			throw new BusinessException(ErrorCode.FILE_TOO_LARGE);
		}
		ImageType type = ImageType.detect(readHeader(file))
				.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_FILE));
		String relative = relativePath(purpose, type);
		Path target = properties.root().resolve(relative);
		try (InputStream in = file.getInputStream()) {
			Files.createDirectories(target.getParent());
			Files.copy(in, target);
		} catch (IOException e) {
			throw new UncheckedIOException("이미지 저장 실패: " + target, e);
		}
		return URL_PREFIX + relative;
	}

	private String relativePath(ImagePurpose purpose, ImageType type) {
		LocalDate today = LocalDate.now(clock);
		return "%s/%d/%02d/%s.%s".formatted(purpose.name().toLowerCase(Locale.ROOT), today.getYear(),
				today.getMonthValue(), UUID.randomUUID(), type.extension());
	}

	private static byte[] readHeader(MultipartFile file) {
		try (InputStream in = file.getInputStream()) {
			return in.readNBytes(ImageType.HEADER_LENGTH);
		} catch (IOException e) {
			throw new BusinessException(ErrorCode.INVALID_FILE);
		}
	}

}
