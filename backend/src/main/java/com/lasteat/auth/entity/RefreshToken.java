package com.lasteat.auth.entity;

import com.lasteat.global.entity.BaseTimeEntity;
import com.lasteat.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 로그인한 기기마다 1개. 재발급하면 이전 행을 지우고 새로 저장한다.
// 원문 토큰은 저장하지 않고 SHA-256 해시(16진수 64자)만 둔다.
@Getter
@Entity
@Table(name = "refresh_token", uniqueConstraints = {
		@UniqueConstraint(name = "uk_refresh_token_hash", columnNames = "token_hash")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends BaseTimeEntity {

	private static final int SHA256_HEX_LENGTH = 64;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Column(nullable = false, length = SHA256_HEX_LENGTH)
	private String tokenHash;

	@Column(nullable = false)
	private LocalDateTime expiresAt;

	@Builder
	private RefreshToken(Member member, String tokenHash, LocalDateTime expiresAt) {
		if (tokenHash == null || tokenHash.length() != SHA256_HEX_LENGTH) {
			throw new IllegalArgumentException("토큰 해시는 SHA-256 16진수 64자여야 합니다.");
		}
		this.member = Objects.requireNonNull(member, "회원이 필요합니다.");
		this.tokenHash = tokenHash;
		this.expiresAt = Objects.requireNonNull(expiresAt, "만료 시각이 필요합니다.");
	}

	public boolean isExpiredAt(LocalDateTime now) {
		return !now.isBefore(expiresAt);
	}

}
