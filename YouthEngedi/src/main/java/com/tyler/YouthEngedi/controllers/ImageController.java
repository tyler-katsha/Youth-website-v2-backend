package com.tyler.YouthEngedi.controllers;

import com.tyler.YouthEngedi.Exceptions.ExplicitContentException;
import com.tyler.YouthEngedi.Exceptions.ImageException;
import com.tyler.YouthEngedi.Exceptions.RateLimitExceededException;
import com.tyler.YouthEngedi.Exceptions.ResourceNotFoundException;
import com.tyler.YouthEngedi.annotations.RateLimited;
import com.tyler.YouthEngedi.models.UserPrincipal;
import com.tyler.YouthEngedi.models.dtos.ApiResult;
import com.tyler.YouthEngedi.models.dtos.FragmentedImage;
import com.tyler.YouthEngedi.services.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api/v1")
@Tag(name="Image Management",description = "Api for fetching and managing images")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService){
        this.imageService = imageService;
    }

    @RateLimited(capacity = 100,tokens = 100,duration = 10,unit = ChronoUnit.SECONDS)
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER','YOUTH_LEADER')")
    @GetMapping("/images")
    @Operation(summary = "Fetches all image objects using Pagination",description = "Fetches all the image objects in the database")
    @ApiResponse(responseCode = "200",description = "Gets every image object")
    @ApiResponse(responseCode = "400",description = "A bad (invalid URL) image was fetched")
    @ApiResponse(responseCode = "500",description = "Something went wrong")
    public ResponseEntity<?> findAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "100") int size){
         try{
             return ResponseEntity.ok(imageService.findAll(page,size));
         } catch (ImageException e){
             return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.BAD_REQUEST);
         } catch (RateLimitExceededException e){
             return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.TOO_MANY_REQUESTS);
         } catch (Exception e){
             return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."),HttpStatus.INTERNAL_SERVER_ERROR);
         }
    }

    @RateLimited(capacity = 100,tokens = 100,duration = 10,unit = ChronoUnit.SECONDS)
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER','YOUTH_LEADER')")
    @PostMapping(value="/images/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "uploads image into Object Storage")
    @ApiResponse(responseCode = "200",description = "Successfully upload image")
    @ApiResponse(responseCode = "400",description = "A bad (invalid URL) image was uploaded")
    @ApiResponse(responseCode = "406",description = "An explict image trying to be uploaded")
    @ApiResponse(responseCode = "500",description = "Something went wrong")
    public ResponseEntity<?> uploadImage(@ModelAttribute MultipartFile image,@AuthenticationPrincipal UserPrincipal principal){
        try{
            return ResponseEntity.ok(imageService.uploadImage(image,principal.getUserId()));
        } catch (ImageException e){
            return new ResponseEntity<>(new ApiResult(false,"Unable to upload image"),HttpStatus.BAD_REQUEST);
        } catch(ExplicitContentException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.NOT_ACCEPTABLE);
        } catch (RateLimitExceededException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.TOO_MANY_REQUESTS);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RateLimited(capacity = 100,tokens = 100,duration = 10,unit = ChronoUnit.SECONDS)
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER','YOUTH_LEADER')")
    @PostMapping(value="/images/upload-chunks",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Split image into fragments and uploads it into Object Storage")
    @ApiResponse(responseCode = "200",description = "Successfully upload image")
    @ApiResponse(responseCode = "400",description = "A bad (invalid URL) image was uploaded")
    @ApiResponse(responseCode = "500",description = "Something went wrong")
    public ResponseEntity<ApiResult> uploadChunks(@ModelAttribute FragmentedImage fragmentedImage){
        try{
            imageService.uploadChunks(fragmentedImage);
            return ResponseEntity.ok(new ApiResult(true,"Image Uploaded"));
        } catch (ImageException e){
            return new ResponseEntity<>(new ApiResult(false,"Unable to upload image"),HttpStatus.BAD_REQUEST);
        } catch (RateLimitExceededException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.TOO_MANY_REQUESTS);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RateLimited(capacity = 100,tokens = 100,duration = 10,unit = ChronoUnit.SECONDS)
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER','YOUTH_LEADER')")
    @DeleteMapping("/images/{id}")
    @Operation(summary = "Deletes image based on id")
    @ApiResponse(responseCode = "200",description = "Successfully deletes image")
    @ApiResponse(responseCode = "404",description = "Image not found based by id")
    @ApiResponse(responseCode = "400",description = "A bad (invalid URL) image was uploaded")
    @ApiResponse(responseCode = "500",description = "Something went wrong")
    public ResponseEntity<ApiResult> deleteImage(@PathVariable long id){
        try{
            imageService.deleteImage(id);
            return ResponseEntity.ok(new ApiResult(true,"Image Deleted"));
        } catch(ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResult(false,"Image not found"),HttpStatus.NOT_FOUND);
        } catch (ImageException e){
            return new ResponseEntity<>(new ApiResult(false,"Unable to upload image"),HttpStatus.BAD_REQUEST);
        } catch (RateLimitExceededException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.TOO_MANY_REQUESTS);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
