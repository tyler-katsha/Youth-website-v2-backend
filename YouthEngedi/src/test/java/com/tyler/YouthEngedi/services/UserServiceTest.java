package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Exceptions.AuthorizationException;
import com.tyler.YouthEngedi.Exceptions.LockedAccountException;
import com.tyler.YouthEngedi.Repository.UserRepository;
import com.tyler.YouthEngedi.jwts.JwtTokenProvider;
import com.tyler.YouthEngedi.models.User;
import com.tyler.YouthEngedi.models.dtos.UserLoginRequest;
import com.tyler.YouthEngedi.models.dtos.UserRegisterRequest;
import com.tyler.YouthEngedi.models.enums.AuthProvider;
import com.tyler.YouthEngedi.models.mappers.UserMapper;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceTest class Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private GenericRedisService redisService;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private final BCryptPasswordEncoder testEncoder = new BCryptPasswordEncoder();

    @Nested
    @DisplayName("Register user unit tests")
    class RegisterUserTests {

        @Test
        @DisplayName("Register user successfully with valid email")
        void registerUserSuccessfullyWithValidEmail() {
            MockMultipartFile profileImage = new MockMultipartFile(
                    "profileImageUrl", "avatar.jpg", "image/jpeg", "image_data".getBytes()
            );

            UserRegisterRequest request = UserRegisterRequest.builder()
                    .email("john.doe@example.com")
                    .password("plainSecret123")
                    .bio("Youth member bio")
                    .profileImageUrl(profileImage)
                    .build();

            User mappedUser = User.builder()
                    .email("john.doe@example.com")
                    .password("plainSecret123")
                    .build();

            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
            when(userMapper.toUser(request)).thenReturn(mappedUser);
            when(cloudinaryService.upload(profileImage)).thenReturn("https://cloudinary.com/avatar.jpg");

            String response = userService.register(request);

            assertEquals("Registration successful", response);

            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();

            assertEquals(AuthProvider.LOCAL, savedUser.getAuthProvider());
            assertTrue(savedUser.isEnabled());
            assertEquals("Youth member bio", savedUser.getBio());
            assertEquals("https://cloudinary.com/avatar.jpg", savedUser.getProfileImageUrl());
            assertTrue(testEncoder.matches("plainSecret123", savedUser.getPassword()));
        }

        @Test
        @DisplayName("Throws an Auth exception if the user email already exist")
        void throwsAnAuthExceptionIfTheUserEmailAlreadyExist() {
            UserRegisterRequest request = UserRegisterRequest.builder()
                    .email("taken@example.com")
                    .password("password123")
                    .build();

            when(userRepository.findByEmail(request.getEmail()))
                    .thenReturn(Optional.of(User.builder().email("taken@example.com").build()));

            AuthorizationException exception = assertThrows(
                    AuthorizationException.class,
                    () -> userService.register(request)
            );

            assertTrue(exception.getMessage().contains("Existing user with email: taken@example.com already exists"));
            verify(userRepository, never()).save(any(User.class));
            verifyNoInteractions(userMapper, cloudinaryService);
        }
    }

    @Nested
    @DisplayName("Login user unit tests")
    class LoginUserTests {

        @Test
        @DisplayName("Logins the user successfully and returns a jwt-token")
        void loginTheUserSuccessfullyAndReturnsAJwtToken() {
            String rawPassword = "mySecurePassword";
            String hashedPassword = testEncoder.encode(rawPassword);

            UserLoginRequest request = UserLoginRequest.builder()
                    .email("jane@example.com")
                    .password(rawPassword)
                    .build();

            User activeUser = User.builder()
                    .id(1L)
                    .email("jane@example.com")
                    .password(hashedPassword)
                    .enabled(true)
                    .build();

            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(activeUser));
            when(tokenProvider.generateToken(activeUser)).thenReturn("mocked.jwt.token");

            String token = userService.login(request);

            assertEquals("mocked.jwt.token", token);
            verify(publisher, times(1)).publishEvent(any(Object.class));
            verify(tokenProvider, times(1)).generateToken(activeUser);
        }

        @Test
        @DisplayName("Throws an Auth Exception for a user not found by email")
        void throwsAnAuthExceptionForAUserNotFoundByEmail() {
            UserLoginRequest request = UserLoginRequest.builder()
                    .email("unknown@example.com")
                    .password("anyPassword")
                    .build();

            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

            AuthorizationException exception = assertThrows(
                    AuthorizationException.class,
                    () -> userService.login(request)
            );

            assertEquals("Invalid credentials", exception.getMessage());
            verifyNoInteractions(publisher, tokenProvider);
        }

        @Test
        @DisplayName("Throws an Auth Exception for a user password not matching")
        void throwsAnAuthExceptionForAUserPasswordNotMatching() {
            String hashedPassword = testEncoder.encode("correctPassword");

            UserLoginRequest request = UserLoginRequest.builder()
                    .email("jane@example.com")
                    .password("wrongPassword")
                    .build();

            User user = User.builder()
                    .id(1L)
                    .email("jane@example.com")
                    .password(hashedPassword)
                    .enabled(true)
                    .build();

            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

            AuthorizationException exception = assertThrows(
                    AuthorizationException.class,
                    () -> userService.login(request)
            );

            assertEquals("Invalid credentials", exception.getMessage());
            verifyNoInteractions(publisher, tokenProvider);
        }

        @Test
        @DisplayName("Throws an LockedAccountException for a user trying to login for a inactive account")
        void throwsAnLockedAccountExceptionForAUserTryingToLoginForAInactiveAccount() {
            UserLoginRequest request = UserLoginRequest.builder()
                    .email("locked@example.com")
                    .password("password123")
                    .build();

            User lockedUser = User.builder()
                    .id(2L)
                    .email("locked@example.com")
                    .password(testEncoder.encode("password123"))
                    .enabled(false)
                    .build();

            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(lockedUser));

            LockedAccountException exception = assertThrows(
                    LockedAccountException.class,
                    () -> userService.login(request)
            );

            assertTrue(exception.getMessage().contains("Account is Locked"));
            verifyNoInteractions(publisher, tokenProvider);
        }
    }
}