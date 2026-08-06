package com.tyler.YouthEngedi.models.mappers;

import com.tyler.YouthEngedi.models.Event;
import com.tyler.YouthEngedi.models.dtos.EventResponse;
import org.mapstruct.Mapper;


@Mapper(componentModel="spring")
public interface EventMapper {

    EventResponse mapToEventResponse(Event event);
}
