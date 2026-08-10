package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Exceptions.ResourceNotFoundException;
import com.tyler.YouthEngedi.Repository.AnnouncementRepository;
import com.tyler.YouthEngedi.Repository.EventRepository;
import com.tyler.YouthEngedi.models.Announcement;
import com.tyler.YouthEngedi.models.Event;
import com.tyler.YouthEngedi.models.dtos.AnnouncementDto;
import com.tyler.YouthEngedi.models.dtos.EventResponse;
import com.tyler.YouthEngedi.models.enums.AnnouncementType;
import com.tyler.YouthEngedi.models.mappers.AnnouncementMapper;
import com.tyler.YouthEngedi.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class AnnouncementService {
    private final AnnouncementRepository announcementRepository;
    private final EventRepository eventRepository;
    private final AnnouncementMapper announcementMapper;

    public AnnouncementService(AnnouncementRepository announcementRepository,EventRepository eventRepository,AnnouncementMapper announcementMapper){
        this.announcementRepository = announcementRepository;
        this.eventRepository = eventRepository;
        this.announcementMapper = announcementMapper;
    }
    public Page<AnnouncementDto> findAll(int page,int size){

        return announcementRepository.findActiveAnnouncements(PageRequest.of(page,size, Sort.by(Sort.Direction.DESC,"createdAt"))).map(announcementMapper::mapToResponse);
    }

    public AnnouncementDto findById(long id) {
        return announcementMapper.mapToResponse(announcementRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Announcement was not found")));
    }

    public void createAnnouncement(AnnouncementDto request){

        announcementRepository.save(Announcement
                .builder()
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType())
                .createdAt(LocalDateTime.now())
                .expiresAt(TimeUtils.convertToDateTime(request.getExpiresAt()))
                .isUrgent(request.isUrgent())
                .build());
    }

    public void updateAnnouncement(AnnouncementDto request){
        Announcement existingAnnouncement = announcementRepository.findById(request.getId()).orElseThrow(() -> new ResourceNotFoundException("Announcement not found"));

        existingAnnouncement.setTitle(request.getTitle());
        existingAnnouncement.setMessage(request.getMessage());
        existingAnnouncement.setType(request.getType());
        existingAnnouncement.setUrgent(request.isUrgent());
        existingAnnouncement.setExpiresAt(TimeUtils.convertToDateTime(request.getExpiresAt()));

        announcementRepository.save(existingAnnouncement);
    }

    public void deleteAnnouncement(long id){
        Announcement existingAnnouncement = announcementRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Announcement not found"));
        announcementRepository.delete(existingAnnouncement);
    }


    // DON'T call this method more than once
    public void tempMethod(){

        List<Event> events = eventRepository.findAll();

        for(Event event:events){

            Announcement announcement = Announcement
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
