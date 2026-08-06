package com.tyler.YouthEngedi.controllers;

import com.tyler.YouthEngedi.Exceptions.ResourceNotFoundException;
import com.tyler.YouthEngedi.models.UserPrincipal;
import com.tyler.YouthEngedi.models.dtos.ApiResult;
import com.tyler.YouthEngedi.models.dtos.EventRequest;
import com.tyler.YouthEngedi.services.EventService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/event")
@RequiredArgsConstructor
@Tag(name="Event Management",description = "Api for fetching and managing events")
public class EventController {

    private final EventService eventService;

    @PreAuthorize("hasAnyRole('ADMIN','YOUTH_LEADER')")
    @PostMapping("/addEvent")
    public ResponseEntity<?> createEvent(@RequestBody EventRequest request, @AuthenticationPrincipal UserPrincipal principal){
        try{
            return ResponseEntity.ok(eventService.createEvent(request,principal.getUserId()));
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."),HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','MEMBER','YOUTH_LEADER')")
    @GetMapping("/events")
    public ResponseEntity<?> findAllEvents(){
        try{
            return ResponseEntity.ok(eventService.findAllEvents());
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."),HttpStatus.BAD_REQUEST);
        }
    }
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER','YOUTH_LEADER')")
    @GetMapping("/events/{date}")
    public ResponseEntity<?> getEventsByDate(@PathVariable String date){
        try{
            return ResponseEntity.ok(eventService.getEventsByDate(date));
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."),HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','YOUTH_LEADER')")
    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<?> removeEvent(@PathVariable long eventId){
        try{
            return ResponseEntity.ok(new ApiResult(true,eventService.removeEvent(eventId)));
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."),HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','YOUTH_LEADER')")
    @PutMapping("/events/{eventId}")
    public ResponseEntity<?> updateEvent(@PathVariable long eventId, @RequestBody EventRequest request,@AuthenticationPrincipal UserPrincipal principal){
        try{
            return ResponseEntity.ok(eventService.updateEvent(eventId,request,principal.getUserId()));
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."),HttpStatus.BAD_REQUEST);
        }
    }


}
