package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Exceptions.ResourceNotFoundException;
import com.tyler.YouthEngedi.Repository.AnnouncementRepository;
import com.tyler.YouthEngedi.Repository.EventRepository;
import com.tyler.YouthEngedi.models.Announcement;
import com.tyler.YouthEngedi.models.Event;
import com.tyler.YouthEngedi.models.User;
import com.tyler.YouthEngedi.models.dtos.AnnouncementDto;
import com.tyler.YouthEngedi.models.dtos.CachedPageResponse;
import com.tyler.YouthEngedi.models.dtos.UserResponse;
import com.tyler.YouthEngedi.models.enums.AnnouncementType;
import com.tyler.YouthEngedi.models.mappers.AnnouncementMapper;
import com.tyler.YouthEngedi.redis.GenericRedisService;
import com.tyler.YouthEngedi.utils.TimeUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AnnouncementService {
    private final AnnouncementRepository announcementRepository;
    private final EventRepository eventRepository;
    private final AnnouncementMapper announcementMapper;
    private final GenericRedisService redisService;

    private static final String ANNOUNCEMENT_PAGE_KEY_PREFIX = "announcement:page:";
    private static final String ANNOUNCEMENT_ID_KEY_PREFIX = "announcement:id:";
    private final static Duration ANNOUNCEMENT_CACHE_TTL = Duration.ofHours(1);
    private static final Duration PAGE_CACHE_TTL = Duration.ofMinutes(15);

    public AnnouncementService(AnnouncementRepository announcementRepository,EventRepository eventRepository,AnnouncementMapper announcementMapper,GenericRedisService redisService){
        this.announcementRepository = announcementRepository;
        this.eventRepository = eventRepository;
        this.announcementMapper = announcementMapper;
        this.redisService = redisService;
    }
    public Page<AnnouncementDto> findAll(int page,int size){
        var cacheKey = ANNOUNCEMENT_PAGE_KEY_PREFIX + page + ":size:" + size;

        var cached = redisService.get(cacheKey, CachedPageResponse.class);

        if (cached.isPresent()) {
            return cached.get().toPage();
        }

        var announcementPage = announcementRepository.findActiveAnnouncements(PageRequest.of(page,size, Sort.by(Sort.Direction.DESC,"createdAt")))
                .map(announcementMapper::mapToResponse);

        var responseToCache = CachedPageResponse.of(announcementPage);

        redisService.set(cacheKey, responseToCache, PAGE_CACHE_TTL);

        return announcementPage;
    }

    public AnnouncementDto findById(long id) {
        var cacheKey = ANNOUNCEMENT_ID_KEY_PREFIX + id;

        var cached = redisService.get(cacheKey, AnnouncementDto.class);

        if (cached.isPresent()) {
            return cached.get();
        }

        var announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement was not found"));

        var response = announcementMapper.mapToResponse(announcement);

        redisService.set(cacheKey, response, ANNOUNCEMENT_CACHE_TTL);

        return response;
    }

    public void createAnnouncement(AnnouncementDto request){

        var cacheKey = ANNOUNCEMENT_ID_KEY_PREFIX + request.getId();

        announcementRepository.save(Announcement
                .builder()
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType())
                .createdAt(LocalDateTime.now())
                .expiresAt(TimeUtils.convertToDateTime(request.getExpiresAt()))
                .isUrgent(request.getIsUrgent())
                .build());

        redisService.deleteByPattern(ANNOUNCEMENT_PAGE_KEY_PREFIX + "*");
        redisService.delete(cacheKey);
    }

    public void updateAnnouncement(AnnouncementDto request){

        var cacheKey = ANNOUNCEMENT_ID_KEY_PREFIX + request.getId();

        Announcement existingAnnouncement = announcementRepository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found"));

        existingAnnouncement.setTitle(request.getTitle());
        existingAnnouncement.setMessage(request.getMessage());
        existingAnnouncement.setType(request.getType());
        existingAnnouncement.setUrgent(request.getIsUrgent());
        existingAnnouncement.setExpiresAt(TimeUtils.convertToDateTime(request.getExpiresAt()));

        announcementRepository.save(existingAnnouncement);

        redisService.deleteByPattern(ANNOUNCEMENT_PAGE_KEY_PREFIX + "*");
        redisService.delete(cacheKey);
    }

    public void deleteAnnouncement(long id){
        var cacheKey = ANNOUNCEMENT_ID_KEY_PREFIX + id;

        var existingAnnouncement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found"));

        announcementRepository.delete(existingAnnouncement);

        redisService.deleteByPattern(ANNOUNCEMENT_PAGE_KEY_PREFIX + "*");
        redisService.delete(cacheKey);
    }

    // DON'T call this method more than once
    public void tempMethod(){

        var events = eventRepository.findAll();

        for(Event event:events){

            var announcement = Announcement
                    .builder()
                    .event(event)
                    .message(event.getDescription())
                    .isUrgent(false)
                    .type(AnnouncementType.EVENT)
                    .createdAt(LocalDateTime.now())
                    .title(event.getTitle())
                    .expiresAt(TimeUtils.getExpiresAt(event.getEventDate()))
                    .build();

            announcementRepository.save(announcement);
        }
    }


}

