package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Exceptions.EventException;
import com.tyler.YouthEngedi.Exceptions.ResourceNotFoundException;
import com.tyler.YouthEngedi.Repository.AnnouncementRepository;
import com.tyler.YouthEngedi.Repository.EventRepository;
import com.tyler.YouthEngedi.Repository.UserRepository;
import com.tyler.YouthEngedi.annotations.LogExecutionTime;
import com.tyler.YouthEngedi.models.Announcement;
import com.tyler.YouthEngedi.models.Event;
import com.tyler.YouthEngedi.models.User;
import com.tyler.YouthEngedi.models.dtos.EventRequest;
import com.tyler.YouthEngedi.models.dtos.EventResponse;
import com.tyler.YouthEngedi.models.enums.AnnouncementType;
import com.tyler.YouthEngedi.models.enums.EventType;
import com.tyler.YouthEngedi.models.mappers.EventMapper;
import com.tyler.YouthEngedi.utils.HtmlTemplate;
import com.tyler.YouthEngedi.utils.TimeUtils;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.tyler.YouthEngedi.constants.UrlConstants.FRONTEND_CALENDER_PROD;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final AnnouncementRepository announcementRepository;
    private final EventMapper eventMapper;

    public EventService(EventRepository eventRepository,UserRepository userRepository,AnnouncementRepository announcementRepository,EventMapper mapper){
        this.announcementRepository = announcementRepository;
        this.eventRepository = eventRepository;
        this.eventMapper = mapper;
        this.userRepository = userRepository;
    }

    @LogExecutionTime(value="Creating event in EventService class",doSave = false)
    public EventResponse createEvent(EventRequest request,long userId){

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        LocalDate dateKey = LocalDate.parse(request.getDateKey());

        Event event = Event.builder().title(request.getTitle()).startTime(request.getStartTime()).endTime(request.getEndTime()).description(request.getDescription()).eventType(EventType.valueOf(request.getEventType())).createdByUserId(user.getId()).eventDate(dateKey).build();

        eventRepository.save(event);

        String emailBody = HtmlTemplate.createEventHtml(event.getTitle(),event.getEventDate(),event.getStartTime(),event.getEndTime());

        String subject = String.format("Event Added: %s", event.getTitle());

//        CompletableFuture.runAsync(() -> {
//                emailService.sendEmail(emailBody,subject);
//        });

        Announcement announcement = Announcement
                .builder()
                .createdAt(LocalDateTime.now())
                .type(AnnouncementType.EVENT)
                .expiresAt(TimeUtils.getExpiresAt(event.getEventDate()))
                .event(event)
                .title(event.getTitle())
                .message(event.getDescription())
                .build();

        announcementRepository.save(announcement);

        return eventMapper.mapToEventResponse(event);
    }

    @LogExecutionTime(value="Get Events by date in EventService class",doSave = false)
    public List<EventResponse> getEventsByDate(String date) {

        LocalDate eventDate = LocalDate.parse(date);
        List<Event> events = eventRepository.findByEventDate(eventDate);

        return events.stream().map(eventMapper::mapToEventResponse).toList();
    }

    @LogExecutionTime(value="Deleting event in EventService class",doSave = false)
    public String removeEvent(long eventId) {

        Event existingEvent = eventRepository.findById(eventId).orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        eventRepository.delete(existingEvent);

        String emailBody = HtmlTemplate.removeEventHtml(existingEvent.getTitle(),existingEvent.getEventDate(),existingEvent.getStartTime(),existingEvent.getEndTime());


        String subject = String.format("Event Cancelled: %s",existingEvent.getTitle());

//        CompletableFuture.runAsync(() -> {
//                emailService.sendEmail(emailBody,subject);
//        });

        Announcement existingAnnouncement = announcementRepository.findByEvent_EventId(eventId).orElseThrow(() -> new EventException("Event is not found or does not exist"));

        announcementRepository.delete(existingAnnouncement);

        return "Event Deleted Successfully";
    }
    @LogExecutionTime(value="Deleting event in EventService class",doSave = false)
    public EventResponse updateEvent(long eventId,EventRequest request,long userId) {

        Event existingEvent = eventRepository.findById(eventId).orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        existingEvent.setEventDate(LocalDate.parse(request.getDateKey()));
        existingEvent.setEventType(EventType.valueOf(request.getEventType()));
        existingEvent.setDescription(request.getDescription());
        existingEvent.setTitle(request.getTitle());
        existingEvent.setEndTime(request.getEndTime());
        existingEvent.setStartTime(request.getStartTime());
        existingEvent.setCreatedByUserId(user.getId());

        Announcement existingAnnouncement = announcementRepository.findByEvent_EventId(eventId).orElseThrow(() -> new EventException("Event is not found or does not exist"));

        existingAnnouncement.setEvent(existingEvent);
        existingAnnouncement.setMessage(existingEvent.getDescription());
        existingAnnouncement.setTitle(existingAnnouncement.getTitle());
        existingAnnouncement.setExpiresAt(TimeUtils.getExpiresAt(existingEvent.getEventDate()));

        return eventMapper.mapToEventResponse(existingEvent);
    }
    public List<EventResponse> findAllEvents() {
        return eventRepository.findAll().stream().map(eventMapper::mapToEventResponse).toList();
    }


}
