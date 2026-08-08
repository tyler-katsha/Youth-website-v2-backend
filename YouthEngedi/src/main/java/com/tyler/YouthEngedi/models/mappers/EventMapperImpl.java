package com.tyler.YouthEngedi.models.mappers;

import com.tyler.YouthEngedi.models.Event;
import com.tyler.YouthEngedi.models.dtos.EventResponse;
import org.springframework.stereotype.Component;

@Component
public class EventMapperImpl implements EventMapper{
    @Override
    public EventResponse mapToEventResponse(Event event) {
        return EventResponse
                .builder()
                .id(event.getEventId())
                .title(event.getTitle())
                .description(event.getDescription())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .dateKey(event.getEventDate().toString())
                .eventType(event.getEventType().name())
                .color(event.getEventType().getValue())
                .build();
    }
}
