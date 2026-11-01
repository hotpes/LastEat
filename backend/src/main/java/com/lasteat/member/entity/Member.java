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

}
