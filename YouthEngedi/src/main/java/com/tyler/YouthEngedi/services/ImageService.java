package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Exceptions.AuthorizationException;
import com.tyler.YouthEngedi.Exceptions.ExplicitContentException;
import com.tyler.YouthEngedi.Exceptions.ResourceNotFoundException;
import com.tyler.YouthEngedi.Repository.ImageRepository;
import com.tyler.YouthEngedi.Repository.UserRepository;
import com.tyler.YouthEngedi.annotations.AuditAction;
import com.tyler.YouthEngedi.models.Image;
import com.tyler.YouthEngedi.models.User;
import com.tyler.YouthEngedi.models.dtos.*;
import com.tyler.YouthEngedi.redis.GenericRedisService;
import com.tyler.YouthEngedi.utils.WebSocketHelper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final CloudinaryService cloudinaryService;
    private final PythonService pythonService;
    private final GenericRedisService redisService;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher publisher;
    private static final String IMAGE_PAGE_KEY_PREFIX = "images:page:";
    private static final Duration PAGE_CACHE_TTL = Duration.ofMinutes(15);

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

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthorizationException("Invalid credentials"));

        var url = cloudinaryService.upload(multipartFile);
        var size = cloudinaryService.getFileFormattedSize(multipartFile);
        var alt = cloudinaryService.generateAltName(multipartFile);

        var request = PredictionRequest.builder()
                .path(url)
                .build();

        var response = pythonService.getPrediction(request);

        if(!response.getApproved()){
            cloudinaryService.deleteImageByUrl(url);
            throw new ExplicitContentException("18+ content is not allowed");
        }

        var image = Image
                .builder()
                .imageUrl(url)
                .alt(alt)
                .createdAt(LocalDateTime.now())
                .postedBy(user.getUsername())
                .imageId(user.getId())
                .flagged(false)
                .size(size)
                .build();


        imageRepository.save(image);

        redisService.deleteByPattern(IMAGE_PAGE_KEY_PREFIX + "*");

        var event = WebSocketHelper.buildImageUpload(image,user);

        publisher.publishEvent(event);

        return "Image was uploaded successfully";
    }

    @AuditAction("Uploading images to Object storage using Cloudinary in chunks")
    public void uploadChunks(FragmentedImage fragmentedImage) {
        cloudinaryService.processChunk(fragmentedImage);
        redisService.deleteByPattern(IMAGE_PAGE_KEY_PREFIX + "*");
    }

    public void deleteImage(long id){
        var image = imageRepository
                .findById(id).orElseThrow(() -> new ResourceNotFoundException("No Image found"));

        imageRepository.delete(image);
        redisService.deleteByPattern(IMAGE_PAGE_KEY_PREFIX + "*");
    }

    @Transactional
    public void flagImage(long id,ImageRequest request) {

        Image image = imageRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Image doesn't exist"));

        User user = userRepository.findById(image.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Image doesn't exist"));

        image.setFlagged(request.isSoftDelete());

        var event = WebSocketHelper.buildImageFlagged(request,image,user);

        publisher.publishEvent(event);

        imageRepository.save(image);
    }
}
