package com.tyler.YouthEngedi.controllers;

import com.tyler.YouthEngedi.Exceptions.EventException;
import com.tyler.YouthEngedi.Exceptions.ResourceNotFoundException;
import com.tyler.YouthEngedi.models.UserPrincipal;
import com.tyler.YouthEngedi.models.dtos.ApiResponse;
import com.tyler.YouthEngedi.models.dtos.EventRequest;
import com.tyler.YouthEngedi.models.dtos.EventResponse;
import com.tyler.YouthEngedi.services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/event")
public class EventController {

    @Autowired
    private EventService eventService;

    @PreAuthorize("hasAnyRole('ADMIN','YOUTH_LEADER')")
    @PostMapping("/addEvent")
    public ResponseEntity<?> createEvent(@RequestBody EventRequest request, @AuthenticationPrincipal UserPrincipal principal){
        try{
            return ResponseEntity.ok(eventService.createEvent(request,principal.getUserId()));
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResponse(false,e.getMessage()),HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResponse(false,"Something went wrong. Please try again later."),HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','MEMBER','YOUTH_LEADER')")
    @GetMapping("/events")
    public ResponseEntity<?> findAllEvents(){
        try{
            return ResponseEntity.ok(eventService.findAllEvents());
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResponse(false,e.getMessage()),HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResponse(false,"Something went wrong. Please try again later."),HttpStatus.BAD_REQUEST);
        }
    }
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER','YOUTH_LEADER')")
    @GetMapping("/events/{date}")
    public ResponseEntity<?> getEventsByDate(@PathVariable String date){
        try{
            return ResponseEntity.ok(eventService.getEventsByDate(date));
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResponse(false,e.getMessage()),HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResponse(false,"Something went wrong. Please try again later."),HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','YOUTH_LEADER')")
    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<?> removeEvent(@PathVariable long eventId){
        try{
            return ResponseEntity.ok(eventService.removeEvent(eventId));
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResponse(false,e.getMessage()),HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResponse(false,"Something went wrong. Please try again later."),HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','YOUTH_LEADER')")
    @PutMapping("/events/{eventId}")
    public ResponseEntity<?> updateEvent(@PathVariable long eventId, @RequestBody EventRequest request,@AuthenticationPrincipal UserPrincipal principal){
        try{
            return ResponseEntity.ok(eventService.updateEvent(eventId,request,principal.getUserId()));
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResponse(false,e.getMessage()),HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResponse(false,"Something went wrong. Please try again later."),HttpStatus.BAD_REQUEST);
        }
    }


}
