package com.tyler.YouthEngedi.controllers;

import com.tyler.YouthEngedi.Exceptions.RateLimitExceededException;
import com.tyler.YouthEngedi.annotations.RateLimited;
import com.tyler.YouthEngedi.models.dtos.ApiResult;
import com.tyler.YouthEngedi.models.events.EventBufferStats;
import com.tyler.YouthEngedi.services.DashboardHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard History Management", description = "Api for fetching recent event logs for user activity")
public class DashboardHistoryController {

    private final DashboardHistoryService historyService;

    public DashboardHistoryController(DashboardHistoryService historyService){
        this.historyService = historyService;
    }

    @RateLimited(unit = ChronoUnit.MINUTES)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/recent")
    @Operation(summary = "Gets recent events",description = "Gets events such as user logged in and logged out for now")
    @ApiResponse(responseCode = "200",description = "Successfully gets the events")
    @ApiResponse(responseCode = "500",description = "Something went wrong")
    public ResponseEntity<?> getRecentEvents(){
        try{
            return ResponseEntity.ok(historyService.getRecentEvents());
        } catch (RateLimitExceededException e){
            return new ResponseEntity<>(e.getMessage(),HttpStatus.TOO_MANY_REQUESTS);
        } catch(Exception e){
            return new ResponseEntity<>("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RateLimited(unit = ChronoUnit.MINUTES)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/stats")
    @Operation(summary = "Gets the size of the buffer of users and the max length of the buffer",description = "Gets the amount of user's currently online")
    @ApiResponse(responseCode = "200", description = "Gets the length of users online")
    public ResponseEntity<EventBufferStats> getEventBufferSize(){
        return ResponseEntity.ok(EventBufferStats.builder()
                .size(historyService.getConnectCountWithGuest())
                .maxEvents(historyService.getMaxEvents())
                .build());
    }
}
