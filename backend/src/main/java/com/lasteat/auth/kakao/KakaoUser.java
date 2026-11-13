package com.lasteat.auth.kakao;

import java.util.Objects;

// 카카오에서 확인한 사용자. email·nickname은 동의 항목에 따라 없을 수 있다.
public record KakaoUser(String id, String email, String nickname) {

	public KakaoUser {
		Objects.requireNonNull(id, "카카오 회원 ID가 필요합니다.");
	}

}
