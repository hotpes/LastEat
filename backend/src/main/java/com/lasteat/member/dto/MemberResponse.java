package com.lasteat.member.dto;

import com.lasteat.member.entity.Member;
import com.lasteat.member.entity.MemberRole;
import io.swagger.v3.oas.annotations.media.Schema;

public record MemberResponse(
		@Schema(example = "12")
		Long id,

		@Schema(example = "맛집탐험가")
		String nickname,

		@Schema(description = "카카오 동의 항목에 따라 null일 수 있음", example = "user@kakao.com")
		String email,

		@Schema(example = "OWNER")
		MemberRole role,

		@Schema(description = "연결된 가게 수. 1 이상이면 마이 화면에 사장님 메뉴 표시", example = "1")
		long ownedStoreCount) {

	public static MemberResponse of(Member member, long ownedStoreCount) {
		return new MemberResponse(member.getId(), member.getNickname(), member.getEmail(), member.getRole(),
				ownedStoreCount);
	}

}
