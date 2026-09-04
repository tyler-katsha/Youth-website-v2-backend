package com.tyler.YouthEngedi.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.cloudinary.utils.ObjectUtils;
import com.tyler.YouthEngedi.Exceptions.ImageException;
import com.tyler.YouthEngedi.Exceptions.ResourceNotFoundException;
import com.tyler.YouthEngedi.Repository.UserRepository;
import com.tyler.YouthEngedi.models.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CloudinaryService Unit Tests")
class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CloudinaryService cloudinaryService;

    @Nested
    @DisplayName("upload(MultipartFile) Tests")
    class UploadTests {

        @Test
        @DisplayName("Should return secure_url when upload succeeds")
        void upload_Success() throws IOException {
            byte[] fileBytes = "test content".getBytes();
            MultipartFile file = new MockMultipartFile("file", "image.jpg", "image/jpeg", fileBytes);
            String expectedUrl = "https://res.cloudinary.com/demo/image/upload/sample.jpg";

            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.upload(eq(fileBytes), any(Map.class)))
                    .thenReturn(Map.of("secure_url", expectedUrl));

            String result = cloudinaryService.upload(file);

            assertEquals(expectedUrl, result);
            verify(uploader).upload(eq(fileBytes), eq(ObjectUtils.asMap("resource_type", "auto")));
        }

        @Test
        @DisplayName("Should throw ImageException when cloudinary upload fails")
        void upload_ThrowsImageException() throws IOException {
            MultipartFile file = new MockMultipartFile("file", "fail.jpg", "image/jpeg", "content".getBytes());

            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.upload(any(byte[].class), any(Map.class)))
                    .thenThrow(new IOException("Network timeout"));

            ImageException exception = assertThrows(ImageException.class, () -> cloudinaryService.upload(file));
            assertTrue(exception.getMessage().contains("Cloudinary upload failed"));
        }
    }

    @Nested
    @DisplayName("upload(MultipartFile, long) Tests")
    class UploadWithUserIdTests {

        @Test
        @DisplayName("Should update user profile image URL and return secure_url")
        void uploadWithUser_Success() throws IOException {
            long userId = 101L;
            byte[] fileBytes = "avatar content".getBytes();
            MultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", fileBytes);
            String expectedUrl = "https://res.cloudinary.com/demo/image/upload/users/101.png";

            User user = new User();
            user.setId(userId);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.upload(eq(fileBytes), any(Map.class)))
                    .thenReturn(Map.of("secure_url", expectedUrl));

            String result = cloudinaryService.upload(file, userId);

            assertEquals(expectedUrl, result);
            assertEquals(expectedUrl, user.getProfileImageUrl());
            verify(userRepository).save(user);
            verify(uploader).upload(eq(fileBytes), eq(ObjectUtils.asMap(
                    "resource_type", "auto",
                    "public_id", "users/" + userId,
                    "overwrite", true
            )));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user does not exist")
        void uploadWithUser_UserNotFound() {
            long userId = 999L;
            MultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "bytes".getBytes());

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> cloudinaryService.upload(file, userId));
            verifyNoInteractions(cloudinary);
        }

        @Test
        @DisplayName("Should return 'Upload failed' when IOException occurs during upload")
        void uploadWithUser_IOExceptionReturnsFailureString() throws IOException {
            long userId = 101L;
            MultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "bytes".getBytes());
            User user = new User();
            user.setId(userId);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.upload(any(byte[].class), any(Map.class))).thenThrow(new IOException("Upload failed"));

            String result = cloudinaryService.upload(file, userId);

            assertEquals("Upload failed", result);
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("generateAltName(MultipartFile) Tests")
    class GenerateAltNameTests {

        @ParameterizedTest(name = "Original: \"{0}\" -> Expected: \"{1}\"")
        @CsvSource({
                "profile_picture_user.png, Profile Picture User",
                "my-awesome-photo.jpeg, My Awesome Photo",
                "simple.jpg, Simple",
                "multi__underscore--dash.png, Multi Underscore Dash"
        })
        @DisplayName("Should properly format file names to title-cased text")
        void generateAltName_FormatsCorrectly(String filename, String expected) {
            MultipartFile file = new MockMultipartFile("file", filename, "image/jpeg", new byte[0]);
            String altName = cloudinaryService.generateAltName(file);
            assertEquals(expected, altName);
        }

        @Test
        @DisplayName("Should return 'Uploaded image' when originalFilename is null or empty")
        void generateAltName_NullFilename() {
            // Test with explicit null
            MultipartFile file = new MockMultipartFile("file", null, "image/jpeg", new byte[0]);
            String altName = cloudinaryService.generateAltName(file);
            assertEquals("Uploaded image", altName);

            // Test with empty string
            MultipartFile emptyFile = new MockMultipartFile("file", "", "image/jpeg", new byte[0]);
            assertEquals("Uploaded image", cloudinaryService.generateAltName(emptyFile));
        }
    }

    @Nested
    @DisplayName("getFileFormattedSize(MultipartFile) Tests")
    class GetFileFormattedSizeTests {

        @Test
        @DisplayName("Should format bytes correctly (< 1024 B)")
        void formatBytes() {
            MultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", new byte[512]);
            assertEquals("512 B", cloudinaryService.getFileFormattedSize(file));
        }

        @Test
        @DisplayName("Should format kilobytes correctly (KB)")
        void formatKilobytes() {
            MultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", new byte[2048]);
            assertEquals("2.00 KB", cloudinaryService.getFileFormattedSize(file));
        }

        @Test
        @DisplayName("Should format megabytes correctly (MB)")
        void formatMegabytes() {
            MultipartFile mockFile = mock(MultipartFile.class);
            when(mockFile.getSize()).thenReturn(5 * 1024 * 1024L); // 5 MB

            assertEquals("5.00 MB", cloudinaryService.getFileFormattedSize(mockFile));
        }

        @Test
        @DisplayName("Should format gigabytes correctly (GB)")
        void formatGigabytes() {
            MultipartFile mockFile = mock(MultipartFile.class);
            when(mockFile.getSize()).thenReturn((long) (1.5 * 1024 * 1024 * 1024L)); // 1.5 GB

            assertEquals("1.50 GB", cloudinaryService.getFileFormattedSize(mockFile));
        }
    }

    @Nested
    @DisplayName("deleteImageByUrl(String) Tests")
    class DeleteImageByUrlTests {

        @Test
        @DisplayName("Should successfully delete image with version prefix")
        void deleteImageByUrl_SuccessWithVersion() throws IOException {
            String url = "https://res.cloudinary.com/demo/image/upload/v1622345678/users/sample_avatar.jpg";
            String expectedPublicId = "users/sample_avatar";

            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.destroy(eq(expectedPublicId), any(Map.class)))
                    .thenReturn(Map.of("result", "ok"));

            boolean result = cloudinaryService.deleteImageByUrl(url);

            assertTrue(result);
            verify(uploader).destroy(eq(expectedPublicId), eq(ObjectUtils.emptyMap()));
        }

        @Test
        @DisplayName("Should successfully delete image without version prefix")
        void deleteImageByUrl_SuccessWithoutVersion() throws IOException {
            String url = "https://res.cloudinary.com/demo/image/upload/sample_avatar.png";
            String expectedPublicId = "sample_avatar";

            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.destroy(eq(expectedPublicId), any(Map.class)))
                    .thenReturn(Map.of("result", "ok"));

            boolean result = cloudinaryService.deleteImageByUrl(url);

            assertTrue(result);
            verify(uploader).destroy(eq(expectedPublicId), eq(ObjectUtils.emptyMap()));
        }

        @Test
        @DisplayName("Should return false when Cloudinary returns non-ok status (e.g. not found)")
        void deleteImageByUrl_CloudinaryReturnsNotFound() throws IOException {
            String url = "https://res.cloudinary.com/demo/image/upload/v12345/sample.jpg";

            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.destroy(eq("sample"), any(Map.class)))
                    .thenReturn(Map.of("result", "not found"));

            boolean result = cloudinaryService.deleteImageByUrl(url);

            assertFalse(result);
        }

        @Test
        @DisplayName("Should return false when URL does not contain '/upload/'")
        void deleteImageByUrl_InvalidUrl() {
            String invalidUrl = "https://res.cloudinary.com/demo/image/download/sample.jpg";

            boolean result = cloudinaryService.deleteImageByUrl(invalidUrl);

            assertFalse(result);
            verifyNoInteractions(cloudinary);
        }

        @Test
        @DisplayName("Should return false when Cloudinary destroy throws IOException")
        void deleteImageByUrl_CloudinaryThrowsIOException() throws IOException {
            String url = "https://res.cloudinary.com/demo/image/upload/sample.jpg";

            when(cloudinary.uploader()).thenReturn(uploader);
            when(uploader.destroy(anyString(), any(Map.class))).thenThrow(new IOException("Timeout"));

            boolean result = cloudinaryService.deleteImageByUrl(url);

            assertFalse(result);
        }
    }
}