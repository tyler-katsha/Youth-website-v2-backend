package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Repository.AuditRepository;
import com.tyler.YouthEngedi.Repository.PerformanceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminServiceTest class Unit Tests")
class AdminServiceTest {

    @Mock
    private AuditRepository auditRepository;
    @Mock
    private PerformanceRepository performanceRepository;

    @InjectMocks
    private AdminService adminService;
}