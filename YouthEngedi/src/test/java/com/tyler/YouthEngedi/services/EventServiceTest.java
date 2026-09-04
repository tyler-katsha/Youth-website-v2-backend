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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventService Unit Tests")
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AnnouncementRepository announcementRepository;

    @Mock
    private EventMapper eventMapper;

    @Mock
    private GenericRedisService redisService;

    @InjectMocks
    private EventService eventService;

    @Captor
    private ArgumentCaptor<Event> eventCaptor;

    @Captor
    private ArgumentCaptor<Announcement> announcementCaptor;

    private static final String EVENT_ALL_KEY = "events:all";

    @Nested
    @DisplayName("createEvent Tests")
    class CreateEventTests {

        @Test
        @DisplayName("Should create event, save announcement, evict cache, and return response")
        void createEvent_Success() {
            long userId = 1L;
            EventRequest request = EventRequest.builder()
                    .title("Youth Night")
                    .description("Fellowship and games")
                    .eventType(EventType.GENERAL.name())
                    .dateKey("2026-10-15")
                    .startTime("18:00")
                    .endTime("20:00")
                    .build();

            User user = User.builder().id(userId).email("lead@test.com").build();
            EventResponse expectedResponse = EventResponse.builder()
                    .title("Youth Night")
                    .description("Fellowship and games")
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(eventMapper.mapToEventResponse(any(Event.class))).thenReturn(expectedResponse);

            EventResponse result = eventService.createEvent(request, userId);

            assertNotNull(result);
            assertEquals(expectedResponse.getTitle(), result.getTitle());

            // Verify Event saved
            verify(eventRepository).save(eventCaptor.capture());
            Event savedEvent = eventCaptor.getValue();
            assertEquals("Youth Night", savedEvent.getTitle());
            assertEquals(EventType.GENERAL, savedEvent.getEventType());
            assertEquals(LocalDate.of(2026, 10, 15), savedEvent.getEventDate());
            assertEquals(userId, savedEvent.getCreatedByUserId());

            // Verify Announcement saved
            verify(announcementRepository).save(announcementCaptor.capture());
            Announcement savedAnnouncement = announcementCaptor.getValue();
            assertEquals(AnnouncementType.EVENT, savedAnnouncement.getType());
            assertEquals("Youth Night", savedAnnouncement.getTitle());
            assertEquals(savedEvent, savedAnnouncement.getEvent());

            // Verify cache eviction
            verify(redisService).delete(EVENT_ALL_KEY);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user is not found")
        void createEvent_UserNotFound_ThrowsException() {
            long userId = 99L;
            EventRequest request = EventRequest.builder().dateKey("2026-10-15").build();

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> eventService.createEvent(request, userId));
            verifyNoInteractions(eventRepository, announcementRepository, redisService, eventMapper);
        }
    }

    @Nested
    @DisplayName("getEventsByDate Tests")
    class GetEventsByDateTests {

        @Test
        @DisplayName("Should parse date and return mapped event responses")
        void getEventsByDate_Success() {
            String dateString = "2026-10-15";
            LocalDate date = LocalDate.parse(dateString);
            Event event1 = Event.builder().title("Event 1").build();
            Event event2 = Event.builder().title("Event 2").build();

            EventResponse response1 = EventResponse.builder().title("Event 1").build();
            EventResponse response2 = EventResponse.builder().title("Event 2").build();

            when(eventRepository.findByEventDate(date)).thenReturn(List.of(event1, event2));
            when(eventMapper.mapToEventResponse(event1)).thenReturn(response1);
            when(eventMapper.mapToEventResponse(event2)).thenReturn(response2);

            List<EventResponse> result = eventService.getEventsByDate(dateString);

            assertEquals(2, result.size());
            assertEquals("Event 1", result.get(0).getTitle());
            assertEquals("Event 2", result.get(1).getTitle());
        }

        @Test
        @DisplayName("Should return empty list when no events exist for given date")
        void getEventsByDate_EmptyList() {
            String dateString = "2026-12-25";
            when(eventRepository.findByEventDate(LocalDate.parse(dateString))).thenReturn(List.of());

            List<EventResponse> result = eventService.getEventsByDate(dateString);

            assertTrue(result.isEmpty());
            verifyNoInteractions(eventMapper);
        }
    }

    @Nested
    @DisplayName("removeEvent Tests")
    class RemoveEventTests {

        @Test
        @DisplayName("Should delete event, delete linked announcement, evict cache, and return success message")
        void removeEvent_Success() {
            long eventId = 10L;
            Event existingEvent = Event.builder().eventId(eventId).title("Camp").build();
            Announcement existingAnnouncement = Announcement.builder().announcementId(5L).event(existingEvent).build();

            when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));
            when(announcementRepository.findByEvent_EventId(eventId)).thenReturn(Optional.of(existingAnnouncement));

            String message = eventService.removeEvent(eventId);

            assertEquals("Event Deleted Successfully", message);
            verify(eventRepository).delete(existingEvent);
            verify(announcementRepository).delete(existingAnnouncement);
            verify(redisService).delete(EVENT_ALL_KEY);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when event does not exist")
        void removeEvent_EventNotFound_ThrowsException() {
            long eventId = 999L;
            when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> eventService.removeEvent(eventId));
            verify(eventRepository, never()).delete(any());
            verifyNoInteractions(announcementRepository, redisService);
        }

        @Test
        @DisplayName("Should throw EventException when linked announcement does not exist")
        void removeEvent_AnnouncementNotFound_ThrowsEventException() {
            long eventId = 10L;
            Event existingEvent = Event.builder().eventId(eventId).title("Camp").build();

            when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));
            when(announcementRepository.findByEvent_EventId(eventId)).thenReturn(Optional.empty());

            assertThrows(EventException.class, () -> eventService.removeEvent(eventId));
            verify(eventRepository).delete(existingEvent);
            verify(announcementRepository, never()).delete(any());
            verify(redisService, never()).delete(anyString());
        }
    }

    @Nested
    @DisplayName("updateEvent Tests")
    class UpdateEventTests {

        @Test
        @DisplayName("Should update event fields, update linked announcement, evict cache, and return mapped response")
        void updateEvent_Success() {
            long eventId = 5L;
            long userId = 2L;

            Event existingEvent = Event.builder()
                    .eventId(eventId)
                    .title("Old Title")
                    .description("Old Desc")
                    .eventType(EventType.ACTIVITY)
                    .build();

            User user = User.builder().id(userId).build();

            Announcement existingAnnouncement = Announcement.builder()
                    .announcementId(50L)
                    .title("Old Announcement Title")
                    .message("Old Desc")
                    .event(existingEvent)
                    .build();

            EventRequest updateRequest = EventRequest.builder()
                    .title("New Title")
                    .description("New Desc")
                    .eventType(EventType.URGENT.name())
                    .dateKey("2026-11-20")
                    .startTime("09:00")
                    .endTime("17:00")
                    .build();

            EventResponse updatedResponse = EventResponse.builder()
                    .title("New Title")
                    .description("New Desc")
                    .build();

            when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(announcementRepository.findByEvent_EventId(eventId)).thenReturn(Optional.of(existingAnnouncement));
            when(eventMapper.mapToEventResponse(existingEvent)).thenReturn(updatedResponse);

            EventResponse result = eventService.updateEvent(eventId, updateRequest, userId);

            assertNotNull(result);
            assertEquals("New Title", result.getTitle());

            // Verify mutated fields on existing entities
            assertEquals("New Title", existingEvent.getTitle());
            assertEquals(EventType.URGENT, existingEvent.getEventType());
            assertEquals(userId, existingEvent.getCreatedByUserId());
            assertEquals("New Desc", existingAnnouncement.getMessage());

            verify(redisService).delete(EVENT_ALL_KEY);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when event to update does not exist")
        void updateEvent_EventNotFound_ThrowsException() {
            long eventId = 999L;
            EventRequest request = EventRequest.builder().build();

            when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> eventService.updateEvent(eventId, request, 1L));
            verifyNoInteractions(announcementRepository, redisService);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user performing update does not exist")
        void updateEvent_UserNotFound_ThrowsException() {
            long eventId = 5L;
            long userId = 999L;
            Event existingEvent = Event.builder().eventId(eventId).build();

            when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> eventService.updateEvent(eventId, EventRequest.builder().build(), userId));
            verifyNoInteractions(announcementRepository, redisService);
        }
    }

    @Nested
    @DisplayName("findAllEvents (Paginated) Tests")
    class FindAllEventsTests {

        private static final String EVENT_PAGE_KEY_PREFIX = "events:page:";
        private static final Duration PAGE_CACHE_TTL = Duration.ofMinutes(15);

        @Test
        @DisplayName("Should return cached page and skip database query when cache is hit")
        void findAllEvents_CacheHit() {
            int page = 0;
            int size = 20;
            String expectedKey = EVENT_PAGE_KEY_PREFIX + page + ":size:" + size;

            List<EventResponse> content = List.of(
                    EventResponse.builder().title("Cached Event 1").build(),
                    EventResponse.builder().title("Cached Event 2").build()
            );
            Page<EventResponse> expectedPage = new PageImpl<>(content, PageRequest.of(page, size), 2);

            CachedPageResponse cachedResponse = mock(CachedPageResponse.class);
            when(cachedResponse.toPage()).thenReturn(expectedPage);
            when(redisService.get(expectedKey, CachedPageResponse.class))
                    .thenReturn(Optional.of(cachedResponse));

            Page<EventResponse> result = eventService.findAllEvents(page, size);

            assertEquals(2, result.getTotalElements());
            assertEquals("Cached Event 1", result.getContent().get(0).getTitle());
            verifyNoInteractions(eventRepository, eventMapper);
            verify(redisService, never()).set(anyString(), any(), any());
        }

        @Test
        @DisplayName("Should fetch from DB pageable, map, store into cache, and return page when cache misses")
        void findAllEvents_CacheMiss() {
            int page = 0;
            int size = 20;
            String expectedKey = EVENT_PAGE_KEY_PREFIX + page + ":size:" + size;
            Pageable pageable = PageRequest.of(page, size);

            Event event1 = Event.builder().title("DB Event 1").build();
            EventResponse response1 = EventResponse.builder().title("DB Event 1").build();
            Page<Event> dbPage = new PageImpl<>(List.of(event1), pageable, 1);

            when(redisService.get(expectedKey, CachedPageResponse.class))
                    .thenReturn(Optional.empty());
            when(eventRepository.findAll(pageable)).thenReturn(dbPage);
            when(eventMapper.mapToEventResponse(event1)).thenReturn(response1);

            Page<EventResponse> result = eventService.findAllEvents(page, size);

            assertEquals(1, result.getTotalElements());
            assertEquals("DB Event 1", result.getContent().get(0).getTitle());

            verify(eventRepository).findAll(pageable);
            verify(eventMapper).mapToEventResponse(event1);
            verify(redisService).set(eq(expectedKey), any(), eq(PAGE_CACHE_TTL));
        }
    }
}