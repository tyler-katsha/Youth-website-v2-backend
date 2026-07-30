package com.tyler.YouthEngedi.Repository;

import com.tyler.YouthEngedi.models.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

    VerificationToken findByToken(String token);
}
