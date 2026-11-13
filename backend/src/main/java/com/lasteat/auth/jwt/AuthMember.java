package com.lasteat.auth.jwt;

import com.lasteat.member.entity.MemberRole;
import java.util.Objects;

// 로그인한 사람. 컨트롤러에서 @AuthenticationPrincipal AuthMember로 받는다.
public record AuthMember(Long memberId, MemberRole role) {

	public AuthMember {
		Objects.requireNonNull(memberId, "회원 ID가 필요합니다.");
		Objects.requireNonNull(role, "회원 역할이 필요합니다.");
	}

}
