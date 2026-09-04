package com.tyler.YouthEngedi.controllers;

import com.tyler.YouthEngedi.Exceptions.ResourceNotFoundException;
import com.tyler.YouthEngedi.models.dtos.AnnouncementDto;
import com.tyler.YouthEngedi.models.dtos.ApiResult;
import com.tyler.YouthEngedi.services.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name="Announcement Management",description = "Api fetching and managing announcements based features")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService){
        this.announcementService = announcementService;
    }

    @GetMapping("/announcements")
    @PreAuthorize("hasAnyRole('ADMIN','YOUTH_LEADER','MEMBER')")
    @Operation(summary = "Finds all the announcements using Pagination",description = "Fetches all the announcements in the database")
    @ApiResponse(responseCode = "200",description = "Announcement was find successfully")
    @ApiResponse(responseCode = "404",description = "No announcements were found")
    @ApiResponse(responseCode = "500",description = "Something went wrong")
    public ResponseEntity<?> findAllAnnouncements(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "30") int size){
        try{
            return ResponseEntity.ok(announcementService.findAll(page,size));
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResult(false,"Failed to verify user"),HttpStatus.NOT_FOUND);
        } catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/announcements/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','YOUTH_LEADER','MEMBER')")
    @Operation(summary = "Finds a announcement based on the announcement id",description = "Fetches announcement based on there unique ID")
    @ApiResponse(responseCode = "200",description = "Returns the announcement by it's Id")
    @ApiResponse(responseCode = "404",description = "Announcement was not found in the database")
    @ApiResponse(responseCode = "500",description = "Something went wrong")
    public ResponseEntity<?> findById(@PathVariable long id){
        try{
            return ResponseEntity.ok(announcementService.findById(id));
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResult(false,"Unable to find announcement"),HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/announcements")
    @PreAuthorize("hasAnyRole('ADMIN','YOUTH_LEADER')")
    @Operation(summary = "Creates an announcement",description = "Creates an announcement and receives partially data from the frontend")
    @ApiResponse(responseCode = "200",description = "Successfully adds an announcement into the database")
    @ApiResponse(responseCode = "500",description = "Something went wrong")
    public ResponseEntity<ApiResult> createAnnouncement(AnnouncementDto request){
        try{
            announcementService.createAnnouncement(request);
            return ResponseEntity.ok(new ApiResult(true,"Successfully created announcement"));
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/announcements")
    @PreAuthorize("hasAnyRole('ADMIN','YOUTH_LEADER')")
    @Operation(summary = "Updates an existing announcement",description = "Updates an existing announcement with new data and replaces the old data")
    @ApiResponse(responseCode = "200",description = "Successfully updates existing announcement with new data")
    @ApiResponse(responseCode = "404",description = "Announcement not found based on id")
    @ApiResponse(responseCode = "500",description = "Something went wrong")
    public ResponseEntity<ApiResult> updateAnnouncement(AnnouncementDto request){
        try{
            announcementService.updateAnnouncement(request);
            return ResponseEntity.ok(new ApiResult(true,"Successfully updated announcement"));
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResult(false,"Announcement doesn't exist"),HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/announcements/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','YOUTH_LEADER')")
    @Operation(summary = "Delete an announcement by id",description = "Delete an announcement based on it's id")
    @ApiResponse(responseCode = "200",description = "Successfully deletes announcement based on id")
    @ApiResponse(responseCode = "404",description = "Announcement not found based on id")
    @ApiResponse(responseCode = "500",description = "Something went wrong")
    public ResponseEntity<ApiResult> deleteAnnouncement(@PathVariable long id){
        try{
            announcementService.deleteAnnouncement(id);
            return ResponseEntity.ok(new ApiResult(true,"Successfully deleted announcement"));
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResult(false,"Announcement doesn't exist"),HttpStatus.NOT_FOUND);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    @PreAuthorize("hasRole('ADMIN')")
//    @GetMapping("/tempControllerMethod")
//    public ResponseEntity<String> TempControllerMethod(){
//        announcementService.tempMethod();
//
//        return ResponseEntity.ok("Successfully excuted temp method");
//    }
}
