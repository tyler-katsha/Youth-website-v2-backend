package com.tyler.YouthEngedi.Repository;

import com.tyler.YouthEngedi.models.ContactSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactSubmissionRepository extends JpaRepository<ContactSubmission,Long> {
}
