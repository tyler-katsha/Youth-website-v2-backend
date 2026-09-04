package com.tyler.YouthEngedi.models.mappers;

import com.tyler.YouthEngedi.models.Announcement;
import com.tyler.YouthEngedi.models.dtos.AnnouncementDto;
import org.mapstruct.Mapper;

@Mapper(componentModel="spring")
public interface AnnouncementMapper {

    AnnouncementDto mapToResponse(Announcement announcement);
}
