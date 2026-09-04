package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Exceptions.ResourceNotFoundException;
import com.tyler.YouthEngedi.Repository.AnnouncementRepository;
import com.tyler.YouthEngedi.Repository.EventRepository;
import com.tyler.YouthEngedi.models.Announcement;
import com.tyler.YouthEngedi.models.Event;
import com.tyler.YouthEngedi.models.dtos.AnnouncementDto;
import com.tyler.YouthEngedi.models.enums.AnnouncementType;
import com.tyler.YouthEngedi.models.mappers.AnnouncementMapper;
import com.tyler.YouthEngedi.redis.GenericRedisService;
import com.tyler.YouthEngedi.utils.TimeUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnnouncementServiceTest class Unit Tests")
class AnnouncementServiceTest {

    @Mock
    private AnnouncementRepository announcementRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private AnnouncementMapper announcementMapper;
    @InjectMocks
    private AnnouncementService announcementService;

    @Test
    @DisplayName("Successfully gets all active announcements")
    void successfullyGetsAllAnnouncements(){

        Announcement announcement = Announcement.builder()
                .announcementId(1L)
                .title("Announcement 1")
                .message("Test 123")
                .type(AnnouncementType.ANNOUNCEMENT)
                .createdAt(LocalDateTime.MIN)
                .expiresAt(LocalDateTime.MAX)
                .isUrgent(false)
                .build();

        AnnouncementDto dto = AnnouncementDto.builder()
                .id(1L)
                .title("Announcement 1")
                .message("Test 123")
                .type(AnnouncementType.ANNOUNCEMENT)
                .createdAt(TimeUtils.formatDateTime(LocalDateTime.MIN))
                .expiresAt(TimeUtils.formatDateTime(LocalDateTime.MAX))
                .isUrgent(false)
                .build();

        Page<Announcement> pageResult = new PageImpl<>(List.of(announcement));

        when(announcementRepository.findActiveAnnouncements((PageRequest) any(Pageable.class))).thenReturn(pageResult);
        when(announcementMapper.mapToResponse(announcement)).thenReturn(dto);

        Page<AnnouncementDto> res = announcementService.findAll(0,20);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        verify(announcementRepository).findActiveAnnouncements((PageRequest) captor.capture());
        verify(announcementMapper).mapToResponse(announcement);

        Pageable pageable = captor.getValue();
        assertEquals(0,pageable.getPageNumber());
        assertEquals(20,pageable.getPageSize());
        assertEquals(Sort.by(Sort.Direction.DESC,"createdAt"),pageable.getSort());

        assertEquals(1,res.getTotalElements());
        assertEquals(dto,res.getContent().getFirst());
    }

    @Test
    @DisplayName("Successfully finds an announcement by id")
    void successfullyFindsAnnouncementById() {
        Announcement announcement = Announcement.builder()
                .announcementId(1L)
                .title("Announcement")
                .build();

        AnnouncementDto dto = AnnouncementDto.builder()
                .id(1L)
                .title("Announcement")
                .build();

        when(announcementRepository.findById(1L))
                .thenReturn(Optional.of(announcement));
        when(announcementMapper.mapToResponse(announcement))
                .thenReturn(dto);

        AnnouncementDto result = announcementService.findById(1L);

        verify(announcementRepository).findById(1L);
        verify(announcementMapper).mapToResponse(announcement);

        assertEquals(dto, result);
    }

    @Test
    @DisplayName("Throws exception when announcement does not exist")
    void throwsExceptionWhenAnnouncementDoesNotExist() {

        when(announcementRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> announcementService.findById(1L)
        );

        assertEquals("Announcement was not found", ex.getMessage());

        verify(announcementRepository).findById(1L);
        verifyNoInteractions(announcementMapper);
    }

    @Test
    @DisplayName("Successfully creates an announcement")
    void successfullyCreatesAnnouncement() {

        AnnouncementDto request = AnnouncementDto.builder()
                .title("Title")
                .message("Message")
                .type(AnnouncementType.ANNOUNCEMENT)
                .expiresAt("2 days")
                .isUrgent(true)
                .build();

        announcementService.createAnnouncement(request);

        ArgumentCaptor<Announcement> captor =
                ArgumentCaptor.forClass(Announcement.class);

        verify(announcementRepository).save(captor.capture());

        Announcement saved = captor.getValue();

        assertEquals(request.getTitle(), saved.getTitle());
        assertEquals(request.getMessage(), saved.getMessage());
        assertEquals(request.getType(), saved.getType());
        assertEquals(request.getIsUrgent(), saved.isUrgent());

        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getExpiresAt());
    }

    @Test
    @DisplayName("Successfully updates an announcement")
    void successfullyUpdatesAnnouncement() {

        Announcement announcement = Announcement.builder()
                .announcementId(1L)
                .title("Old")
                .message("Old")
                .type(AnnouncementType.EVENT)
                .isUrgent(false)
                .build();

        AnnouncementDto request = AnnouncementDto.builder()
                .id(1L)
                .title("New")
                .message("New Message")
                .type(AnnouncementType.ANNOUNCEMENT)
                .expiresAt("2 days")
                .isUrgent(true)
                .build();

        when(announcementRepository.findById(1L))
                .thenReturn(Optional.of(announcement));

        announcementService.updateAnnouncement(request);

        verify(announcementRepository).findById(1L);
        verify(announcementRepository).save(announcement);

        assertEquals("New", announcement.getTitle());
        assertEquals("New Message", announcement.getMessage());
        assertEquals(AnnouncementType.ANNOUNCEMENT, announcement.getType());
        assertTrue(announcement.isUrgent());
        assertNotNull(announcement.getExpiresAt());
    }

    @Test
    @DisplayName("Throws exception when updating a missing announcement")
    void throwsExceptionWhenUpdatingMissingAnnouncement() {

        AnnouncementDto request = AnnouncementDto.builder()
                .id(1L)
                .build();

        when(announcementRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> announcementService.updateAnnouncement(request)
        );

        assertEquals("Announcement not found", ex.getMessage());

        verify(announcementRepository).findById(1L);
        verify(announcementRepository, never()).save(any());
    }

    @Test
    @DisplayName("Successfully deletes an announcement")
    void successfullyDeletesAnnouncement() {

        Announcement announcement = Announcement.builder()
                .announcementId(1L)
                .build();

        when(announcementRepository.findById(1L))
                .thenReturn(Optional.of(announcement));

        announcementService.deleteAnnouncement(1L);

        verify(announcementRepository).findById(1L);
        verify(announcementRepository).delete(announcement);
    }

    @Test
    @DisplayName("Throws exception when deleting a missing announcement")
    void throwsExceptionWhenDeletingMissingAnnouncement() {

        when(announcementRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> announcementService.deleteAnnouncement(1L)
        );

        assertEquals("Announcement not found", ex.getMessage());

        verify(announcementRepository).findById(1L);
        verify(announcementRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Successfully creates announcements from all events")
    void successfullyCreatesAnnouncementsFromEvents() {

        Event event1 = Event.builder()
                .title("Event 1")
                .description("Description 1")
                .eventDate(LocalDate.now().plusDays(3))
                .build();

        Event event2 = Event.builder()
                .title("Event 2")
                .description("Description 2")
                .eventDate(LocalDate.now().plusDays(5))
                .build();

        when(eventRepository.findAll())
                .thenReturn(List.of(event1, event2));

        announcementService.tempMethod();

        ArgumentCaptor<Announcement> captor =
                ArgumentCaptor.forClass(Announcement.class);

        verify(eventRepository).findAll();
        verify(announcementRepository, times(2))
                .save(captor.capture());

        List<Announcement> saved = captor.getAllValues();

        assertEquals(2, saved.size());

        assertEquals("Event 1", saved.get(0).getTitle());
        assertEquals("Description 1", saved.get(0).getMessage());
        assertEquals(AnnouncementType.EVENT, saved.get(0).getType());

        assertEquals("Event 2", saved.get(1).getTitle());
        assertEquals("Description 2", saved.get(1).getMessage());
        assertEquals(AnnouncementType.EVENT, saved.get(1).getType());
    }
}