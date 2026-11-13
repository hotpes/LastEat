package com.lasteat.auth.kakao;

public interface KakaoClient {

	// 카카오 액세스 토큰으로 사용자를 확인한다. 실패하면 BusinessException(KAKAO_AUTH_FAILED)
	KakaoUser fetchUser(String kakaoAccessToken);

}
