package com.tyler.YouthEngedi.services;

import com.cloudinary.Cloudinary;
import com.tyler.YouthEngedi.Repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CloudinaryServiceTest class Unit Tests")
class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CloudinaryService cloudinaryService;
}