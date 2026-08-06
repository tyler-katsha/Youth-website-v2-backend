package com.tyler.YouthEngedi.controllers;

import com.tyler.YouthEngedi.Exceptions.ResourceNotFoundException;
import com.tyler.YouthEngedi.models.dtos.AnnouncementDto;
import com.tyler.YouthEngedi.models.dtos.ApiResult;
import com.tyler.YouthEngedi.services.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping("/announcements")
    @PreAuthorize("hasAnyRole('ADMIN','YOUTH_LEADER','MEMBER')")
    public ResponseEntity<?> findAllAnnouncements(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "30") int size){
        try{
            return ResponseEntity.ok(announcementService.findAll(page,size));
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResult(false,"Failed to verify user"),HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/announcements/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','YOUTH_LEADER','MEMBER')")
    public ResponseEntity<?> findById(@PathVariable long id){
        try{
            return ResponseEntity.ok(announcementService.findById(id));
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResult(false,"Unable to find announcement"),HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/announcements")
    @PreAuthorize("hasAnyRole('ADMIN','YOUTH_LEADER')")
    public ResponseEntity<ApiResult> createAnnouncement(AnnouncementDto request){
        try{
            announcementService.createAnnouncement(request);
            return ResponseEntity.ok(new ApiResult(true,"Successfully created announcement"));
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResult(false,"Failed to create a announcement"),HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/announcements")
    @PreAuthorize("hasAnyRole('ADMIN','YOUTH_LEADER')")
    public ResponseEntity<ApiResult> updateAnnouncement(AnnouncementDto request){
        try{
            announcementService.updateAnnouncement(request);
            return ResponseEntity.ok(new ApiResult(true,"Successfully updated announcement"));
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResult(false,"Announcement doesn't exist"),HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/announcements/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','YOUTH_LEADER')")
    public ResponseEntity<ApiResult> deleteAnnouncement(@PathVariable long id){
        try{
            announcementService.deleteAnnouncement(id);
            return ResponseEntity.ok(new ApiResult(true,"Successfully deleted announcement"));
        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResult(false,"Announcement doesn't exist"),HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."), HttpStatus.BAD_REQUEST);
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
