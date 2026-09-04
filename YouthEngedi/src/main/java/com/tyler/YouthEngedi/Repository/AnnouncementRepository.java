package com.tyler.YouthEngedi.Repository;

import com.tyler.YouthEngedi.models.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement,Long> {

    @Query("SELECT a FROM Announcement a WHERE a.expiresAt > CURRENT_TIMESTAMP")
    Page<Announcement> findActiveAnnouncements(PageRequest pageRequest);

    Optional<Announcement> findByEvent_EventId(long eventId);
}
