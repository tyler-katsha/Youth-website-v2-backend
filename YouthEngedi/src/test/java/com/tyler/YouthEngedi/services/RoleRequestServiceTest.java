package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Repository.RoleRequestRepository;
import com.tyler.YouthEngedi.Repository.UserRepository;
import com.tyler.YouthEngedi.models.mappers.RoleRequestMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
@DisplayName("RoleRequestServiceTest class Unit Tests")

class RoleRequestServiceTest {
    @Mock
    private RoleRequestRepository repository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;
    @Mock
    private RoleRequestMapper roleRequestMapper;
    @InjectMocks
    private RoleRequestService roleRequestService;
}