package com.lasteat.auth.dto;

import com.lasteat.member.entity.Member;
import com.lasteat.member.entity.MemberRole;

public record LoginResponse(String accessToken, String refreshToken, boolean newMember, MemberSummary member) {

	public static LoginResponse of(TokenResponse tokens, boolean newMember, Member member) {
		return new LoginResponse(tokens.accessToken(), tokens.refreshToken(), newMember, MemberSummary.from(member));
	}

	public record MemberSummary(Long id, String nickname, MemberRole role) {

		static MemberSummary from(Member member) {
			return new MemberSummary(member.getId(), member.getNickname(), member.getRole());
		}

	}

}
