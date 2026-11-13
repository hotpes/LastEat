package com.lasteat.auth.repository;

import com.lasteat.auth.entity.RefreshToken;
import com.lasteat.member.entity.Member;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	Optional<RefreshToken> findByTokenHash(String tokenHash);

	// 지운 행 수를 돌려준다. 같은 토큰으로 동시에 재발급하면 한쪽만 1을 받는다
	@Modifying(flushAutomatically = true)
	@Query("delete from RefreshToken r where r.id = :id")
	int deleteByIdReturningCount(@Param("id") Long id);

	@Modifying(flushAutomatically = true)
	@Query("delete from RefreshToken r where r.tokenHash = :tokenHash")
	int deleteByTokenHash(@Param("tokenHash") String tokenHash);

	@Modifying(flushAutomatically = true)
	@Query("delete from RefreshToken r where r.member = :member and r.expiresAt <= :now")
	int deleteExpired(@Param("member") Member member, @Param("now") LocalDateTime now);

}
