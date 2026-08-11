package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Repository.UserRepository;
import com.tyler.YouthEngedi.jwts.JwtTokenProvider;
import com.tyler.YouthEngedi.models.mappers.UserMapper;
import org.junit.jupiter.api.DisplayName;
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
}