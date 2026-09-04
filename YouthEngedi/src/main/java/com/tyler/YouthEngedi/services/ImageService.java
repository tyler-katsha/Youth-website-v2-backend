package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Exceptions.ExplicitContentException;
import com.tyler.YouthEngedi.Exceptions.ResourceNotFoundException;
import com.tyler.YouthEngedi.Repository.ImageRepository;
import com.tyler.YouthEngedi.annotations.AuditAction;
import com.tyler.YouthEngedi.models.Image;
import com.tyler.YouthEngedi.models.dtos.*;
import com.tyler.YouthEngedi.redis.GenericRedisService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class ImageService {

    private final ImageRepository imageRepository;
    private final CloudinaryService cloudinaryService;
    private final PythonService pythonService;
    private final GenericRedisService redisService;

    private static final String IMAGE_PAGE_KEY_PREFIX = "images:page:";
    private static final Duration PAGE_CACHE_TTL = Duration.ofMinutes(15);

    public ImageService(ImageRepository imageRepository,CloudinaryService cloudinaryService, PythonService pythonService,GenericRedisService redisService){
        this.imageRepository = imageRepository;
        this.cloudinaryService = cloudinaryService;
        this.pythonService = pythonService;
        this.redisService = redisService;
    }

    public Page<Image> findAll(int page,int size){

        var cacheKey = IMAGE_PAGE_KEY_PREFIX + page + ":size:" + size;

        var cached = redisService.get(cacheKey, CachedPageResponse.class);

        if (cached.isPresent()) {
            return cached.get().toPage();
        }

        var imagePage = imageRepository.findAll(PageRequest.of(page,size));

        var responseToCache = CachedPageResponse.of(imagePage);

        redisService.set(cacheKey, responseToCache, PAGE_CACHE_TTL);

        return imagePage;
    }

    @AuditAction("Uploading images to Object storage using Cloudinary")
    public String uploadImage(MultipartFile multipartFile,long userId) {

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
                .postedBy(userId)
                .size(size)
                .build();


        imageRepository.save(image);

        redisService.deleteByPattern(IMAGE_PAGE_KEY_PREFIX + "*");

        return "Image/s uploaded";
    }

    @AuditAction("Uploading images to Object storage using Cloudinary in chunks")
    public void uploadChunks(FragmentedImage fragmentedImage) {
        cloudinaryService.processChunk(fragmentedImage);
        redisService.deleteByPattern(IMAGE_PAGE_KEY_PREFIX + "*");
    }

    public void deleteImage(long id){
        Image image = imageRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No Image found"));
        imageRepository.delete(image);
        redisService.deleteByPattern(IMAGE_PAGE_KEY_PREFIX + "*");
    }
}
