package com.tyler.YouthEngedi.controllers;

import com.tyler.YouthEngedi.Exceptions.ExplicitContentException;
import com.tyler.YouthEngedi.Exceptions.ImageException;
import com.tyler.YouthEngedi.Exceptions.ResourceNotFoundException;
import com.tyler.YouthEngedi.models.dtos.ApiResult;
import com.tyler.YouthEngedi.models.dtos.FragmentedImage;
import com.tyler.YouthEngedi.services.ImageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
@Tag(name="Image Management",description = "Api for fetching and managing images")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService){
        this.imageService = imageService;
    }
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER','YOUTH_LEADER')")
    @GetMapping("/images")
    public ResponseEntity<?> findAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "100") int size){
         try{
             return ResponseEntity.ok(imageService.findAll(page,size));
         } catch (ImageException e){
             return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.BAD_REQUEST);
         } catch (Exception e){
             return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."),HttpStatus.INTERNAL_SERVER_ERROR);
         }
    }
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER','YOUTH_LEADER')")
    @PostMapping(value="/images/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImage(@ModelAttribute MultipartFile image){
        try{
            return ResponseEntity.ok(imageService.uploadImage(image));
        } catch (ImageException e){
            return new ResponseEntity<>(new ApiResult(false,"Unable to upload image"),HttpStatus.BAD_REQUEST);
        } catch(ExplicitContentException e){
            return new ResponseEntity<>(new ApiResult(false,e.getMessage()),HttpStatus.NOT_ACCEPTABLE);
        }catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','MEMBER','YOUTH_LEADER')")
    @PostMapping(value="/images/upload-chunks",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResult> uploadChunks(@ModelAttribute FragmentedImage fragmentedImage){
        try{
            imageService.uploadChunks(fragmentedImage);
            return ResponseEntity.ok(new ApiResult(true,"Image Uploaded"));
        } catch (ImageException e){
            return new ResponseEntity<>(new ApiResult(false,"Unable to upload image"),HttpStatus.BAD_REQUEST);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','MEMBER','YOUTH_LEADER')")
    @DeleteMapping("/images/{id}")
    public ResponseEntity<ApiResult> deleteImage(@PathVariable long id){
        try{
            imageService.deleteImage(id);
            return ResponseEntity.ok(new ApiResult(true,"Image Deleted"));
        } catch(ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResult(false,"Image not found"),HttpStatus.NOT_FOUND);
        }catch (ImageException e){
            return new ResponseEntity<>(new ApiResult(false,"Unable to upload image"),HttpStatus.BAD_REQUEST);
        } catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(new ApiResult(false,"Something went wrong. Please try again later."),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
