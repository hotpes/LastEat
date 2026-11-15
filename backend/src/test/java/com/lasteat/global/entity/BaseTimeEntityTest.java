package com.lasteat.global.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.lasteat.TestcontainersConfiguration;
import com.lasteat.global.config.JpaAuditingConfig;
import com.lasteat.member.entity.AuthProvider;
import com.lasteat.member.entity.Member;
import com.lasteat.member.entity.MemberRole;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JpaAuditingConfig.class})
class BaseTimeEntityTest {

	@Autowired
	private TestEntityManager em;

	@Test
	void 저장하면_생성일시와_수정일시가_채워진다() {
		LocalDateTime before = LocalDateTime.now().minusSeconds(1);

		Member saved = em.persistFlushFind(Member.builder()
				.provider(AuthProvider.KAKAO)
				.providerId("audit-test")
				.nickname("테스트")
				.role(MemberRole.CONSUMER)
				.build());

		assertThat(saved.getCreatedAt()).isAfter(before);
		assertThat(saved.getUpdatedAt()).isAfter(before);
	}

}
