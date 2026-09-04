package com.tyler.YouthEngedi.controllers;

import com.tyler.YouthEngedi.Exceptions.RateLimitExceededException;
import com.tyler.YouthEngedi.Exceptions.ResourceNotFoundException;
import com.tyler.YouthEngedi.annotations.RateLimited;
import com.tyler.YouthEngedi.models.UserPrincipal;
import com.tyler.YouthEngedi.models.dtos.ApiResult;
import com.tyler.YouthEngedi.models.dtos.EventRequest;
import com.tyler.YouthEngedi.services.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api/v1/event")
@Tag(name="Event Management",description = "Api for fetching and managing events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService){
        this.eventService = eventService;
    }

    @RateLimited(unit = ChronoUnit.MINUTES)
    @PreAuthorize("hasAnyRole('ADMIN','YOUTH_LEADER')")
    @PostMapping("/addEvent")
    @Operation(summary = "Creates an event",description = "Creates an event and receives partially data from the frontend")
    @ApiResponse(responseCode = "200",description = "Successfully adds an event into the database")
    @ApiResponse(responseCode = "500",description = "Something went wrong")
    public ResponseEntity<?> createEvent(@RequestBody EventRequest request, @AuthenticationPrincipal UserPrincipal principal){
        try{
            return ResponseEntity.ok(eventService.createEvent(request,principal.getUserId()));
        } catch (RateLimitExceededException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.TOO_MANY_REQUESTS);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."),HttpStatus.BAD_REQUEST);
        }
    }

    @RateLimited(unit = ChronoUnit.MINUTES)
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER','YOUTH_LEADER')")
    @GetMapping("/events")
    @Operation(summary = "Gets all the events")
    @ApiResponse(responseCode = "200",description = "Successfully fetches all events")
    @ApiResponse(responseCode = "404",description = "No events were found")
    @ApiResponse(responseCode = "500",description = "Something went wrong")
    public ResponseEntity<?> findAllEvents(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "100") int size){
        try{
            return ResponseEntity.ok(eventService.findAllEvents(page,size));
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.NOT_FOUND);
        } catch (RateLimitExceededException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.TOO_MANY_REQUESTS);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RateLimited(unit = ChronoUnit.MINUTES)
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER','YOUTH_LEADER')")
    @GetMapping("/events/{date}")
    @Operation(summary = "Get event based on the current date")
    @ApiResponse(responseCode = "200",description = "Successfully fetches event based on the date")
    @ApiResponse(responseCode = "404",description = "No event was found")
    @ApiResponse(responseCode = "500",description = "Something went wrong")
    public ResponseEntity<?> getEventsByDate(@PathVariable String date){
        try{
            return ResponseEntity.ok(eventService.getEventsByDate(date));
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.NOT_FOUND);
        } catch (RateLimitExceededException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.TOO_MANY_REQUESTS);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RateLimited(unit = ChronoUnit.MINUTES)
    @PreAuthorize("hasAnyRole('ADMIN','YOUTH_LEADER')")
    @DeleteMapping("/events/{eventId}")
    @Operation(summary = "Deletes an event based on the event id")
    @ApiResponse(responseCode = "200",description = "Successfully deletes event based on it's id")
    @ApiResponse(responseCode = "404",description = "No event was found")
    @ApiResponse(responseCode = "500",description = "Something went wrong")
    public ResponseEntity<?> removeEvent(@PathVariable long eventId){
        try{
            return ResponseEntity.ok(new ApiResult(true,eventService.removeEvent(eventId)));
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.NOT_FOUND);
        } catch (RateLimitExceededException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.TOO_MANY_REQUESTS);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RateLimited(unit = ChronoUnit.MINUTES)
    @PreAuthorize("hasAnyRole('ADMIN','YOUTH_LEADER')")
    @PutMapping("/events/{eventId}")
    @Operation(summary = "Updates existing event",description = "Updates the existing event data with new data")
    @ApiResponse(responseCode = "200",description = "Successfully updates existing event")
    @ApiResponse(responseCode = "404",description = "No event was found")
    @ApiResponse(responseCode = "500",description = "Something went wrong")
    public ResponseEntity<?> updateEvent(@PathVariable long eventId, @RequestBody EventRequest request,@AuthenticationPrincipal UserPrincipal principal){
        try{
            return ResponseEntity.ok(eventService.updateEvent(eventId,request,principal.getUserId()));
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.NOT_FOUND);
        } catch (RateLimitExceededException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.TOO_MANY_REQUESTS);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
