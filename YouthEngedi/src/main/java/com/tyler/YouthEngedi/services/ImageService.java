package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Exceptions.ExplicitContentException;
import com.tyler.YouthEngedi.Exceptions.ResourceNotFoundException;
import com.tyler.YouthEngedi.Repository.ImageRepository;
import com.tyler.YouthEngedi.annotations.AuditAction;
import com.tyler.YouthEngedi.annotations.LogExecutionTime;
import com.tyler.YouthEngedi.models.Image;
import com.tyler.YouthEngedi.models.dtos.FragmentedImage;
import com.tyler.YouthEngedi.models.dtos.PredictionRequest;
import com.tyler.YouthEngedi.models.dtos.PredictionResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
public class ImageService {

    private final ImageRepository imageRepository;
    private final CloudinaryService cloudinaryService;
    private final PythonService pythonService;

    public ImageService(ImageRepository imageRepository,CloudinaryService cloudinaryService, PythonService pythonService){
        this.imageRepository = imageRepository;
        this.cloudinaryService = cloudinaryService;
        this.pythonService = pythonService;
    }

    public Page<Image> findAll(int page,int size){
        return imageRepository.findAll(PageRequest.of(page,size));
    }

    @AuditAction("Uploading images to Object storage using Cloudinary")
    @LogExecutionTime("Uploading images to Object storage using Cloudinary")
    public String uploadImage(MultipartFile multipartFile) {

        String url = cloudinaryService.upload(multipartFile);
        String size = cloudinaryService.getFileFormattedSize(multipartFile);
        String alt = cloudinaryService.generateAltName(multipartFile);

        PredictionRequest request = PredictionRequest.builder().path(url).build();
        PredictionResponse response = pythonService.getPrediction(request);

        if(!response.isApproved()){
            cloudinaryService.deleteImageByUrl(url);
            throw new ExplicitContentException("18+ content is not allowed");
        }

        Image image = Image
                .builder()
                .imageUrl(url)
                .alt(alt)
                .createdAt(LocalDateTime.now())
                .size(size)
                .build();


        imageRepository.save(image);

        return "Image/s uploaded";
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
