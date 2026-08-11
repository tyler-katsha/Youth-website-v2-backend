package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Repository.AnnouncementRepository;
import com.tyler.YouthEngedi.Repository.EventRepository;
import com.tyler.YouthEngedi.models.mappers.AnnouncementMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

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
}