package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Repository.ImageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImageServiceTest class Unit Tests")
class ImageServiceTest {

    @Mock
    private ImageRepository imageRepository;
    @Mock
    private CloudinaryService cloudinaryService;
    @Mock
    private PythonService pythonService;
    @InjectMocks
    private ImageService imageService;
}