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
import com.tyler.YouthEngedi.utils.TimeUtils;
import jakarta.mail.MessagingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class EventService {

    private final static Logger logger = LogManager.getLogger(EventService.class);
    private final static String link = "http://localhost:5173/calendar";

    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AnnouncementRepository announcementRepository;


    @LogExecutionTime(value="Creating event in EventService class",doSave = false)
    public EventResponse createEvent(EventRequest request,long userId){

        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        LocalDate dateKey = LocalDate.parse(request.getDateKey());

        Event event = Event.builder().title(request.getTitle()).startTime(request.getStartTime()).endTime(request.getEndTime()).description(request.getDescription()).eventType(EventType.valueOf(request.getEventType())).createdByUserId(user.getId()).eventDate(dateKey).build();

        eventRepository.save(event);

        String emailBody = String.format("""
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="UTF-8">
    </head>
    <body style="margin:0;padding:20px;background:#f4f4f4;font-family:Arial,sans-serif;">

        <table width="100%%" cellpadding="0" cellspacing="0">
            <tr>
                <td align="center">

                    <table width="600" cellpadding="0" cellspacing="0"
                           style="background:#ffffff;border-radius:8px;overflow:hidden;
                                  box-shadow:0 2px 8px rgba(0,0,0,0.1);">

                        <tr>
                            <td style="background:#16a34a;padding:25px;text-align:center;color:#ffffff;">
                                <h1 style="margin:0;">New Event Added</h1>
                            </td>
                        </tr>

                        <tr>
                            <td style="padding:30px;color:#333333;line-height:1.6;">

                                <p>Hello,</p>

                                <p>
                                    A new Youth Engedi event has been added to the calendar. We would love for you to join us!
                                </p>

                                <table width="100%%" cellpadding="10" cellspacing="0"
                                       style="border:1px solid #e5e7eb;background:#f9fafb;border-radius:6px;">

                                    <tr>
                                        <td><strong>Event</strong></td>
                                        <td>%s</td>
                                    </tr>

                                    <tr>
                                        <td><strong>Date</strong></td>
                                        <td>%s</td>
                                    </tr>

                                    <tr>
                                        <td><strong>Time</strong></td>
                                        <td>%s - %s</td>
                                    </tr>

                                </table>

                                <p style="margin-top:25px;">
                                    We look forward to seeing you there. Don't forget to invite your friends and be part of what God is doing through our youth ministry!
                                </p>

                                <p>
                                    Click the button below to view the full event details in Youth Engedi.
                                </p>

                                <p>
                                    <a href="%s"
                                       style="display:inline-block;padding:12px 20px;
                                              background:#2563eb;color:white;
                                              text-decoration:none;border-radius:5px;">
                                        View Event
                                    </a>
                                </p>

                                <p>
                                    See you soon!
                                </p>

                                <p>
                                    God bless,<br>
                                    <strong>Engedi Youth Ministry</strong>
                                </p>

                            </td>
                        </tr>

                        <tr>
                            <td style="padding:15px;background:#f3f4f6;
                                       text-align:center;font-size:12px;color:#6b7280;">
                                This is an automated message from the Youth Engedi Management System.
                            </td>
                        </tr>

                    </table>

                </td>
            </tr>
        </table>

    </body>
    </html>
    """,
                event.getTitle(),
                event.getEventDate(),
                event.getStartTime(),
                event.getEndTime(),
                link
        );

        String subject = String.format("Event Added: %s", event.getTitle());

        CompletableFuture.runAsync(() -> {
            try{
                emailService.sendEmail(emailBody,subject);
            } catch (MessagingException e){
                // change null to user before going to production
                logger.error("Failed to send email to {} with subject {}","Everyone",subject,e);
            }
        });

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

        return mapToEventResponse(event);
    }

    @LogExecutionTime(value="Get Events by date in EventService class",doSave = false)
    public List<EventResponse> getEventsByDate(String date) {

        LocalDate eventDate = LocalDate.parse(date);
        List<Event> events = eventRepository.findByEventDate(eventDate);

        return events.stream().map(this::mapToEventResponse).toList();
    }

    @LogExecutionTime(value="Deleting event in EventService class",doSave = false)
    public String removeEvent(long eventId) {

        Event existingEvent = eventRepository.findById(eventId).orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        eventRepository.delete(existingEvent);

        String emailBody = String.format("""
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
        </head>
        <body style="margin:0;padding:0;background:#f4f4f4;font-family:Arial,sans-serif;">
            <table width="100%%" cellpadding="0" cellspacing="0" style="padding:30px 0;">
                <tr>
                    <td align="center">
                        <table width="600" cellpadding="0" cellspacing="0"
                               style="background:#ffffff;border-radius:10px;overflow:hidden;
                                      box-shadow:0 2px 8px rgba(0,0,0,0.1);">

                            <tr>
                                <td style="background:#dc2626;padding:25px;text-align:center;color:white;">
                                    <h1 style="margin:0;">Event Cancelled</h1>
                                </td>
                            </tr>

                            <tr>
                                <td style="padding:30px;color:#333333;line-height:1.6;">

                                    <p>Hello,</p>

                                    <p>
                                        We regret to inform you that the following event has been cancelled:
                                    </p>

                                    <table width="100%%" cellpadding="10" cellspacing="0"
                                           style="background:#f9fafb;border:1px solid #e5e7eb;
                                                  border-radius:8px;margin:20px 0;">
                                        <tr>
                                            <td><strong>Event</strong></td>
                                            <td>%s</td>
                                        </tr>
                                        <tr>
                                            <td><strong>Date</strong></td>
                                            <td>%s</td>
                                        </tr>
                                        <tr>
                                            <td><strong>Time</strong></td>
                                            <td>%s - %s</td>
                                        </tr>
                                    </table>

                                    <p>
                                        We sincerely apologize for any inconvenience this may cause.
                                        Thank you for your understanding, and we hope to see you at
                                        our future events.
                                    </p>

                                    <p>
                                        If you have any questions, please feel free to contact the
                                        church leadership.
                                    </p>

                                    <p>
                                        God bless,<br>
                                        <strong>Engedi Youth Ministry</strong>
                                    </p>

                                </td>
                            </tr>

                            <tr>
                                <td style="background:#f3f4f6;padding:15px;text-align:center;
                                           font-size:12px;color:#6b7280;">
                                    This is an automated message from the Engedi Youth Management System.
                                </td>
                            </tr>

                        </table>
                    </td>
                </tr>
            </table>
        </body>
        </html>
        """,
                existingEvent.getTitle(),
                existingEvent.getEventDate(),
                existingEvent.getStartTime(),
                existingEvent.getEndTime()
        );

        String subject = String.format("Event Cancelled: %s",existingEvent.getTitle());

        CompletableFuture.runAsync(() -> {
            try{
                emailService.sendEmail(emailBody,subject);
            } catch (MessagingException e){
                logger.error("Failed to send email to {} with subject {}","Everyone",subject,e);
            }
        });

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
        return mapToEventResponse(existingEvent);
    }

    @LogExecutionTime(value="Convert Event type to EventResponse type in EventService class")
    private EventResponse mapToEventResponse(Event event){
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

    public List<EventResponse> findAllEvents() {
        try{

            List<Event> events = eventRepository.findAll();

            return events.stream().map(this::mapToEventResponse).toList();
        } catch (IllegalArgumentException e){
            e.printStackTrace();
        }

        return List.of();
    }


}
