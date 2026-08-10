package com.tyler.YouthEngedi.models.mappers;

import com.tyler.YouthEngedi.models.Announcement;
import com.tyler.YouthEngedi.models.dtos.AnnouncementDto;
import com.tyler.YouthEngedi.utils.TimeUtils;
import org.springframework.stereotype.Component;

@Component
public class AnnouncementMapperImpl implements AnnouncementMapper{

    public AnnouncementDto mapToResponse(Announcement announcement){
        return AnnouncementDto
                .builder()
                .id(announcement.getAnnouncementId())
                .title(announcement.getTitle())
                .message(announcement.getMessage())
                .type(announcement.getType())
                .createdAt(TimeUtils.getRemainingTimeText(announcement.getCreatedAt()))
                .expiresAt(TimeUtils.getRemainingTimeText(announcement.getExpiresAt()))
                .isUrgent(announcement.isUrgent())
                .build();
    }
}
