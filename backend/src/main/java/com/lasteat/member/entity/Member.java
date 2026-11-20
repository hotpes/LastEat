package com.lasteat.member.entity;

import com.lasteat.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "member", uniqueConstraints = {
		@UniqueConstraint(name = "uk_member_provider", columnNames = {"provider", "provider_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

	public static final String WITHDRAWN_NICKNAME = "탈퇴한 회원";

	// 카카오 회원번호는 숫자뿐이라 겹치지 않는다
	private static final String WITHDRAWN_PROVIDER_ID_PREFIX = "withdrawn-";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(nullable = false, length = 20)
	private AuthProvider provider;

	@Column(nullable = false)
	private String providerId;

	private String email;

	@Column(nullable = false, length = 30)
	private String nickname;

	@Enumerated(EnumType.STRING)
	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(nullable = false, length = 20)
	private MemberRole role;

	private String fcmToken;

	private LocalDateTime withdrawnAt;

	@Builder
	private Member(AuthProvider provider, String providerId, String email, String nickname, MemberRole role) {
		this.provider = provider;
		this.providerId = providerId;
		this.email = email;
		this.nickname = nickname;
		this.role = role;
	}

	public void updateFcmToken(String fcmToken) {
		this.fcmToken = fcmToken;
	}

	// 탈퇴. 지난 예약이 회원을 참조하므로 행은 남기고 개인정보만 지운다 (V5).
	// providerId를 바꿔 두므로 같은 카카오 계정으로 다시 로그인하면 새 회원으로 가입된다.
	public void withdraw(LocalDateTime now) {
		this.providerId = WITHDRAWN_PROVIDER_ID_PREFIX + id;
		this.email = null;
		this.nickname = WITHDRAWN_NICKNAME;
		this.fcmToken = null;
		this.withdrawnAt = Objects.requireNonNull(now, "탈퇴 시각이 필요합니다.");
	}

	// 사장님 등록(국세청 진위확인 통과) 뒤 사장님으로 바꾼다. 이미 사장님이면 그대로
	public void promoteToOwner() {
		this.role = MemberRole.OWNER;
	}

	public boolean isWithdrawn() {
		return withdrawnAt != null;
	}

}
