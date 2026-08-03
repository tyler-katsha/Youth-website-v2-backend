package com.tyler.YouthEngedi.controllers;

import com.tyler.YouthEngedi.Exceptions.ImageException;
import com.tyler.YouthEngedi.Exceptions.ResourceNotFoundException;
import com.tyler.YouthEngedi.models.dtos.ApiResponse;
import com.tyler.YouthEngedi.models.dtos.FragmentedImage;
import com.tyler.YouthEngedi.services.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
public class ImageController {

    @Autowired
    private ImageService imageService;

    @PreAuthorize("hasAnyRole('ADMIN','MEMBER','YOUTH_LEADER')")
    @GetMapping("/images")
    public ResponseEntity<?> findAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "100") int size){
         try{
             return imageService.findAll(page,size);
         } catch (ImageException e){
             return new ResponseEntity<>(new ApiResponse(false,e.getMessage()),HttpStatus.BAD_REQUEST);
         } catch (Exception e){
             return new ResponseEntity<>(new ApiResponse(false,"Something went wrong. Please try again later."),HttpStatus.INTERNAL_SERVER_ERROR);
         }
    }
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER','YOUTH_LEADER')")
    @PostMapping(value="/images/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImage(@ModelAttribute MultipartFile image){
        try{
            return imageService.uploadImage(image);
        } catch (ImageException e){
            return new ResponseEntity<>(new ApiResponse(false,"Unable to upload image"),HttpStatus.BAD_REQUEST);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResponse(false,"Something went wrong. Please try again later."),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','MEMBER','YOUTH_LEADER')")
    @PostMapping(value="/images/upload-chunks",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadChunks(@ModelAttribute FragmentedImage fragmentedImage){
        try{
            imageService.uploadChunks(fragmentedImage);
            return ResponseEntity.ok("Image Uploaded");
        } catch (ImageException e){
            return new ResponseEntity<>(new ApiResponse(false,"Unable to upload image"),HttpStatus.BAD_REQUEST);
        } catch (Exception e){
            return new ResponseEntity<>(new ApiResponse(false,"Something went wrong. Please try again later."),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','MEMBER','YOUTH_LEADER')")
    @DeleteMapping("/images/{id}")
    public ResponseEntity<?> deleteImage(@PathVariable long id){
        try{
            imageService.deleteImage(id);
            return ResponseEntity.ok("Image Deleted");
        } catch(ResourceNotFoundException e){
            return new ResponseEntity<>(new ApiResponse(false,"Image not found"),HttpStatus.NOT_FOUND);
        }catch (ImageException e){
            return new ResponseEntity<>(new ApiResponse(false,"Unable to upload image"),HttpStatus.BAD_REQUEST);
        } catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(new ApiResponse(false,"Something went wrong. Please try again later."),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
