package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Exceptions.ExplicitContentException;
import com.tyler.YouthEngedi.Exceptions.ResourceNotFoundException;
import com.tyler.YouthEngedi.Repository.ImageRepository;
import com.tyler.YouthEngedi.models.Image;
import com.tyler.YouthEngedi.models.dtos.CachedPageResponse;
import com.tyler.YouthEngedi.models.dtos.FragmentedImage;
import com.tyler.YouthEngedi.models.dtos.PredictionRequest;
import com.tyler.YouthEngedi.models.dtos.PredictionResponse;
import com.tyler.YouthEngedi.redis.GenericRedisService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImageService Unit Tests")
class ImageServiceTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private PythonService pythonService;

    @Mock
    private GenericRedisService redisService;

    @InjectMocks
    private ImageService imageService;

    @Captor
    private ArgumentCaptor<Image> imageCaptor;

    private static final String IMAGE_PAGE_KEY_PREFIX = "images:page:";
    private static final Duration PAGE_CACHE_TTL = Duration.ofMinutes(15);

    @Nested
    @DisplayName("findAll Tests")
    class FindAllTests {

        @Test
        @DisplayName("Should return cached page when cache hit occurs")
        void findAll_CacheHit() {
            int page = 0;
            int size = 10;
            String cacheKey = IMAGE_PAGE_KEY_PREFIX + page + ":size:" + size;

            Image image = Image.builder().imageId(1L).imageUrl("https://cloud.com/1.jpg").build();
            Page<Image> expectedPage = new PageImpl<>(List.of(image), PageRequest.of(page, size), 1);

            CachedPageResponse<Image> cachedResponse = mock(CachedPageResponse.class);
            when(cachedResponse.toPage()).thenReturn(expectedPage);
            when(redisService.get(cacheKey, CachedPageResponse.class)).thenReturn(Optional.of(cachedResponse));

            Page<Image> result = imageService.findAll(page, size);

            assertEquals(1, result.getContent().size());
            assertEquals("https://cloud.com/1.jpg", result.getContent().get(0).getImageUrl());
            verify(cachedResponse).toPage();
            verifyNoInteractions(imageRepository);
            verify(redisService, never()).set(anyString(), any(), any());
        }

        @Test
        @DisplayName("Should fetch from DB, cache result, and return page when cache miss occurs")
        void findAll_CacheMiss() {
            int page = 0;
            int size = 10;
            String cacheKey = IMAGE_PAGE_KEY_PREFIX + page + ":size:" + size;

            Image image = Image.builder().imageId(2L).imageUrl("https://cloud.com/2.jpg").build();
            Page<Image> dbPage = new PageImpl<>(List.of(image), PageRequest.of(page, size), 1);

            when(redisService.get(cacheKey, CachedPageResponse.class)).thenReturn(Optional.empty());
            when(imageRepository.findAll(PageRequest.of(page, size))).thenReturn(dbPage);

            Page<Image> result = imageService.findAll(page, size);

            assertEquals(1, result.getContent().size());
            verify(imageRepository).findAll(PageRequest.of(page, size));
            verify(redisService).set(eq(cacheKey), any(CachedPageResponse.class), eq(PAGE_CACHE_TTL));
        }
    }

    @Nested
    @DisplayName("uploadImage Tests")
    class UploadImageTests {

        @Test
        @DisplayName("Should save image, evict cache, and return success message when image is approved")
        void uploadImage_Approved_Success() {
            long userId = 42L;
            MultipartFile file = new MockMultipartFile("file", "pic.png", "image/png", "test-bytes".getBytes());
            String url = "https://cloudinary.com/sample.png";
            String formattedSize = "1.50 MB";
            String altName = "Pic";

            when(cloudinaryService.upload(file)).thenReturn(url);
            when(cloudinaryService.getFileFormattedSize(file)).thenReturn(formattedSize);
            when(cloudinaryService.generateAltName(file)).thenReturn(altName);

            PredictionResponse approvedResponse = PredictionResponse.builder().approved(true).build();
            when(pythonService.getPrediction(any(PredictionRequest.class))).thenReturn(approvedResponse);

            String result = imageService.uploadImage(file, userId);

            assertEquals("Image/s uploaded", result);

            verify(imageRepository).save(imageCaptor.capture());
            Image savedImage = imageCaptor.getValue();
            assertEquals(url, savedImage.getImageUrl());
            assertEquals(altName, savedImage.getAlt());
            assertEquals(formattedSize, savedImage.getSize());
            assertEquals(userId, savedImage.getPostedBy());
            assertNotNull(savedImage.getCreatedAt());

            verify(redisService).deleteByPattern(IMAGE_PAGE_KEY_PREFIX + "*");
            verify(cloudinaryService, never()).deleteImageByUrl(anyString());
        }

        @Test
        @DisplayName("Should delete uploaded Cloudinary image and throw ExplicitContentException when image is rejected")
        void uploadImage_Rejected_ThrowsExplicitContentException() {
            long userId = 42L;
            MultipartFile file = new MockMultipartFile("file", "nsfw.png", "image/png", "bad-bytes".getBytes());
            String url = "https://cloudinary.com/nsfw.png";

            when(cloudinaryService.upload(file)).thenReturn(url);
            when(cloudinaryService.getFileFormattedSize(file)).thenReturn("500 KB");
            when(cloudinaryService.generateAltName(file)).thenReturn("Nsfw");

            PredictionResponse rejectedResponse = PredictionResponse.builder().approved(false).build();
            when(pythonService.getPrediction(any(PredictionRequest.class))).thenReturn(rejectedResponse);

            ExplicitContentException ex = assertThrows(
                    ExplicitContentException.class,
                    () -> imageService.uploadImage(file, userId)
            );

            assertEquals("18+ content is not allowed", ex.getMessage());
            verify(cloudinaryService).deleteImageByUrl(url);
            verifyNoInteractions(imageRepository);
            verify(redisService, never()).deleteByPattern(anyString());
        }
    }

    @Nested
    @DisplayName("uploadChunks Tests")
    class UploadChunksTests {

        @Test
        @DisplayName("Should forward fragmented image to Cloudinary and invalidate Redis page cache")
        void uploadChunks_Success() {
            FragmentedImage fragmentedImage = new FragmentedImage();

            imageService.uploadChunks(fragmentedImage);

            verify(cloudinaryService).processChunk(fragmentedImage);
            verify(redisService).deleteByPattern(IMAGE_PAGE_KEY_PREFIX + "*");
        }
    }

    @Nested
    @DisplayName("deleteImage Tests")
    class DeleteImageTests {

        @Test
        @DisplayName("Should delete image and invalidate Redis page cache when image exists")
        void deleteImage_Success() {
            long imageId = 7L;
            Image image = Image.builder().imageId(imageId).imageUrl("https://cloud.com/sample.jpg").build();

            when(imageRepository.findById(imageId)).thenReturn(Optional.of(image));

            imageService.deleteImage(imageId);

            verify(imageRepository).delete(image);
            verify(redisService).deleteByPattern(IMAGE_PAGE_KEY_PREFIX + "*");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when image does not exist")
        void deleteImage_NotFound_ThrowsException() {
            long imageId = 999L;

            when(imageRepository.findById(imageId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> imageService.deleteImage(imageId));
            verify(imageRepository, never()).delete(any());
            verify(redisService, never()).deleteByPattern(anyString());
        }
    }
}