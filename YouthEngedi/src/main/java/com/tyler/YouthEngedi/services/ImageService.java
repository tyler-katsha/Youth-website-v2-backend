package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Exceptions.ResourceNotFoundException;
import com.tyler.YouthEngedi.Repository.ImageRepository;
import com.tyler.YouthEngedi.annotations.AuditAction;
import com.tyler.YouthEngedi.annotations.LogExecutionTime;
import com.tyler.YouthEngedi.models.Image;
import com.tyler.YouthEngedi.models.dtos.FragmentedImage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final CloudinaryService cloudinaryService;

    public Page<Image> findAll(int page,int size){
        return imageRepository.findAll(PageRequest.of(page,size));
    }

    @AuditAction("Uploading images to Object storage using Cloudinary")
    @LogExecutionTime(value="Uploading images to Object storage using Cloudinary")
    public String uploadImage(MultipartFile multipartFile) {

        String url = cloudinaryService.upload(multipartFile);
        String size = cloudinaryService.getFileFormattedSize(multipartFile);
        String alt = cloudinaryService.generateAltName(multipartFile);

        Image image = Image
                .builder()
                .imageUrl(url)
                .alt(alt)
                .createdAt(LocalDateTime.now())
                .size(size)
                .build();
        imageRepository.save(image);

        return "Image uploaded to database";
    }

    @AuditAction("Uploading images to Object storage using Cloudinary in chunks")
    @LogExecutionTime(value="Uploading images to Object storage using Cloudinary in chunks")
    public void uploadChunks(FragmentedImage fragmentedImage) {
        cloudinaryService.processChunk(fragmentedImage);
    }

    @LogExecutionTime(value="Removing image by id from Object storage",doSave = false)
    public void deleteImage(long id){
        Image image = imageRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No Image found"));
        imageRepository.delete(image);
    }
}
