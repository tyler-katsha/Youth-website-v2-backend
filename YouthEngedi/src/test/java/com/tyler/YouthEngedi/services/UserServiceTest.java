package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Repository.UserRepository;
import com.tyler.YouthEngedi.jwts.JwtTokenProvider;
import com.tyler.YouthEngedi.models.mappers.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


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
    private CookieService cookieService;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("Register user unit tests")
    class RegisterUserTests{

        @Test
        @DisplayName("Register user successfully with valid email")
        void registerUserSuccessfullyWithValidEmail(){

        }

        @Test
        @DisplayName("Throws an Auth exception if the user email already exist")
        void throwsAnAuthExceptionIfTheUserEmailAlreadyExist(){

        }
    }

    @Nested
    @DisplayName("Login user unit tests")
    class LoginUserTests{

        @Test
        @DisplayName("Logins the user successfully and returns a jwt-token")
        void loginTheUserSuccessfullyAndReturnsAJwtToken(){

        }

        @Test
        @DisplayName("Throws an Auth Exception for a user not found by email")
        void throwsAnAuthExceptionForAUserNotFoundByEmail(){

        }

        @Test
        @DisplayName("Throws an Auth Exception for a user password not matching")
        void throwsAnAuthExceptionForAUserPasswordNotMatching(){

        }

        @Test
        @DisplayName("Throws an LockedAccountException for a user trying to login for a inactive account")
        void throwsAnLockedAccountExceptionForAUserTryingToLoginForAInactiveAccount(){

        }
    }
}