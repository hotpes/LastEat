package com.lasteat.member.repository;

import com.lasteat.member.entity.AuthProvider;
import com.lasteat.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

	Optional<Member> findByProviderAndProviderId(AuthProvider provider, String providerId);

}
