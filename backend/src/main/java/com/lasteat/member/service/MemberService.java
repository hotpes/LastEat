package com.lasteat.member.service;

import com.lasteat.auth.repository.RefreshTokenRepository;
import com.lasteat.bookmark.repository.BookmarkRepository;
import com.lasteat.global.exception.BusinessException;
import com.lasteat.global.exception.ErrorCode;
import com.lasteat.member.dto.MemberResponse;
import com.lasteat.member.entity.Member;
import com.lasteat.member.repository.MemberRepository;
import com.lasteat.store.repository.StoreRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 내 정보·푸시 토큰·탈퇴. 탈퇴했거나 없는 회원의 토큰이면 INVALID_TOKEN (accessToken은 최대 1시간 남아 있을 수 있다)
@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

	private final MemberRepository memberRepository;

	private final StoreRepository storeRepository;

	private final BookmarkRepository bookmarkRepository;

	private final RefreshTokenRepository refreshTokenRepository;

	private final Clock clock;

	@Transactional(readOnly = true)
	public MemberResponse getMe(Long memberId) {
		Member member = findActive(memberId);
		return MemberResponse.of(member, storeRepository.countByOwnerId(memberId));
	}

	public void updateFcmToken(Long memberId, String fcmToken) {
		findActive(memberId).updateFcmToken(fcmToken.strip());
	}

	// 저장한 가게와 모든 기기의 토큰을 지우고 개인정보를 비운다. 지난·진행 중 예약은 기록으로 남는다
	// (받지 않은 예약은 수령 마감 30분 뒤 노쇼 처리되며 재고가 돌아간다).
	public void withdraw(Long memberId) {
		Member member = findActive(memberId);
		if (storeRepository.countByOwnerId(memberId) > 0) {
			throw new BusinessException(ErrorCode.OWNER_HAS_STORE);
		}
		bookmarkRepository.deleteAllByMemberId(memberId);
		refreshTokenRepository.deleteAllByMemberId(memberId);
		member.withdraw(LocalDateTime.now(clock));
	}

	private Member findActive(Long memberId) {
		return memberRepository.findById(memberId)
				.filter(member -> !member.isWithdrawn())
				.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));
	}

}
