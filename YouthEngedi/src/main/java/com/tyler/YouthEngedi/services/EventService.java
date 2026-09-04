package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Exceptions.EventException;
import com.tyler.YouthEngedi.Exceptions.ResourceNotFoundException;
import com.tyler.YouthEngedi.Repository.AnnouncementRepository;
import com.tyler.YouthEngedi.Repository.EventRepository;
import com.tyler.YouthEngedi.Repository.UserRepository;
import com.tyler.YouthEngedi.models.Announcement;
import com.tyler.YouthEngedi.models.Event;
import com.tyler.YouthEngedi.models.User;
import com.tyler.YouthEngedi.models.dtos.EventRequest;
import com.tyler.YouthEngedi.models.dtos.EventResponse;
import com.tyler.YouthEngedi.models.enums.AnnouncementType;
import com.tyler.YouthEngedi.models.enums.EventType;
import com.tyler.YouthEngedi.models.mappers.EventMapper;
import com.tyler.YouthEngedi.redis.GenericRedisService;
import com.tyler.YouthEngedi.utils.TimeUtils;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final AnnouncementRepository announcementRepository;
    private final EventMapper eventMapper;
    private final GenericRedisService redisService;

    private final static String EVENT_ALL_KEY = "events:all";
    private static final String EVENT_ID_KEY_PREFIX = "event:id:";
    private final static Duration EVENT_CACHE_TTL = Duration.ofHours(1);

    public EventService(EventRepository eventRepository,UserRepository userRepository,AnnouncementRepository announcementRepository,EventMapper mapper,GenericRedisService redisService){
        this.announcementRepository = announcementRepository;
        this.eventRepository = eventRepository;
        this.eventMapper = mapper;
        this.userRepository = userRepository;
        this.redisService = redisService;
    }

    public EventResponse createEvent(EventRequest request,long userId){

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        var dateKey = LocalDate.parse(request.getDateKey());

        var event = Event.builder()
                .title(request.getTitle())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .description(request.getDescription())
                .eventType(EventType.valueOf(request.getEventType()))
                .createdByUserId(user.getId())
                .eventDate(dateKey)
                .build();

        eventRepository.save(event);

        var announcement = Announcement
                .builder()
                .createdAt(LocalDateTime.now())
                .type(AnnouncementType.EVENT)
                .expiresAt(TimeUtils.getExpiresAt(event.getEventDate()))
                .event(event)
                .title(event.getTitle())
                .message(event.getDescription())
                .build();

        announcementRepository.save(announcement);

        redisService.delete(EVENT_ALL_KEY);

        return eventMapper.mapToEventResponse(event);
    }

    public List<EventResponse> getEventsByDate(String date) {

        var eventDate = LocalDate.parse(date);

        return eventRepository.findByEventDate(eventDate)
                .stream()
                .map(eventMapper::mapToEventResponse)
                .toList();
    }

    public String removeEvent(long eventId) {

        Event existingEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        eventRepository.delete(existingEvent);

        Announcement existingAnnouncement = announcementRepository.findByEvent_EventId(eventId)
                .orElseThrow(() -> new EventException("Event is not found or does not exist"));

        announcementRepository.delete(existingAnnouncement);

        redisService.delete(EVENT_ALL_KEY);

        return "Event Deleted Successfully";
    }

    public EventResponse updateEvent(long eventId,EventRequest request,long userId) {

        Event existingEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        existingEvent.setEventDate(LocalDate.parse(request.getDateKey()));
        existingEvent.setEventType(EventType.valueOf(request.getEventType()));
        existingEvent.setDescription(request.getDescription());
        existingEvent.setTitle(request.getTitle());
        existingEvent.setEndTime(request.getEndTime());
        existingEvent.setStartTime(request.getStartTime());
        existingEvent.setCreatedByUserId(user.getId());

        Announcement existingAnnouncement = announcementRepository.findByEvent_EventId(eventId)
                .orElseThrow(() -> new EventException("Event is not found or does not exist"));

        existingAnnouncement.setEvent(existingEvent);
        existingAnnouncement.setMessage(existingEvent.getDescription());
        existingAnnouncement.setTitle(existingAnnouncement.getTitle());
        existingAnnouncement.setExpiresAt(TimeUtils.getExpiresAt(existingEvent.getEventDate()));

        redisService.delete(EVENT_ALL_KEY);

        return eventMapper.mapToEventResponse(existingEvent);
    }

    public List<EventResponse> findAllEvents() {

        return eventRepository.findAll()
                .stream()
                .map(eventMapper::mapToEventResponse)
                .toList();
    }


}
