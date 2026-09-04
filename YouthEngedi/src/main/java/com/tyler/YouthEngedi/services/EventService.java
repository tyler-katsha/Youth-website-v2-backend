package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Exceptions.EventException;
import com.tyler.YouthEngedi.Exceptions.ResourceNotFoundException;
import com.tyler.YouthEngedi.Repository.AnnouncementRepository;
import com.tyler.YouthEngedi.Repository.EventRepository;
import com.tyler.YouthEngedi.Repository.UserRepository;
import com.tyler.YouthEngedi.models.Announcement;
import com.tyler.YouthEngedi.models.Event;
import com.tyler.YouthEngedi.models.User;
import com.tyler.YouthEngedi.models.dtos.CachedPageResponse;
import com.tyler.YouthEngedi.models.dtos.EventRequest;
import com.tyler.YouthEngedi.models.dtos.EventResponse;
import com.tyler.YouthEngedi.models.enums.AnnouncementType;
import com.tyler.YouthEngedi.models.enums.EventType;
import com.tyler.YouthEngedi.models.mappers.EventMapper;
import com.tyler.YouthEngedi.redis.GenericRedisService;
import com.tyler.YouthEngedi.utils.TimeUtils;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    private static final String EVENT_PAGE_KEY_PREFIX = "events:page:";
    private static final String EVENT_ID_KEY_PREFIX = "event:id:";
    private final static Duration EVENT_CACHE_TTL = Duration.ofHours(1);
    private static final Duration PAGE_CACHE_TTL = Duration.ofMinutes(15);

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

        redisService.deleteByPattern(EVENT_PAGE_KEY_PREFIX + "*");

        return eventMapper.mapToEventResponse(event);
    }

    public List<EventResponse> getEventsByDate(String date) {

        var eventDate = LocalDate.parse(date);

        return eventRepository.findByEventDate(eventDate)
                .stream()
                .map(eventMapper::mapToEventResponse)
                .toList();
    }

    @Transactional
    public String removeEvent(long eventId) {

        var cacheKey = EVENT_ID_KEY_PREFIX + eventId;

        Announcement existingAnnouncement = announcementRepository.findByEvent_EventId(eventId)
                .orElseThrow(() -> new EventException("Event is not found or does not exist"));

        announcementRepository.delete(existingAnnouncement);

        Event existingEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        eventRepository.delete(existingEvent);



        redisService.deleteByPattern(EVENT_PAGE_KEY_PREFIX + "*");
        redisService.delete(cacheKey);

        return "Event Deleted Successfully";
    }

    public EventResponse updateEvent(long eventId,EventRequest request,long userId) {

        var cacheKey = EVENT_ID_KEY_PREFIX + eventId;

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

        redisService.deleteByPattern(EVENT_PAGE_KEY_PREFIX + "*");
        redisService.delete(cacheKey);

        return eventMapper.mapToEventResponse(existingEvent);
    }

    public Page<EventResponse> findAllEvents(int page, int size) {

        String cacheKey = EVENT_PAGE_KEY_PREFIX + page + ":size:" + size;

        var cached = redisService.get(cacheKey, CachedPageResponse.class);

        if (cached.isPresent()) {
            return cached.get().toPage();
        }

        var pageable = PageRequest.of(page,size);

        var eventPage = eventRepository.findAll(pageable).map(eventMapper::mapToEventResponse);

        var responseToCache = CachedPageResponse.of(eventPage);

        redisService.set(cacheKey, responseToCache, PAGE_CACHE_TTL);

        return eventPage;
    }


}
