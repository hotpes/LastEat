package com.lasteat.auth.token;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

// refreshToken 원문 생성과 DB 저장용 해시. 원문은 앱에만 있고 DB에는 SHA-256 해시만 둔다.
public final class RefreshTokens {

	private static final int TOKEN_BYTES = 32;

	private static final SecureRandom RANDOM = new SecureRandom();

	private RefreshTokens() {
	}

	public static String generate() {
		byte[] bytes = new byte[TOKEN_BYTES];
		RANDOM.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	public static String hash(String rawToken) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", e);
		}
	}

}
