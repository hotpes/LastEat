package com.lasteat.global.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.lasteat.TestcontainersConfiguration;
import com.lasteat.global.config.JpaAuditingConfig;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

		AuditedSample saved = em.persistFlushFind(new AuditedSample());

		assertThat(saved.getCreatedAt()).isAfter(before);
		assertThat(saved.getUpdatedAt()).isAfter(before);
	}

	@Entity
	static class AuditedSample extends BaseTimeEntity {

		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		private Long id;

	}

}
