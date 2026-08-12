package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Repository.AuditRepository;
import com.tyler.YouthEngedi.Repository.PerformanceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminServiceTest class Unit Tests")
class AdminServiceTest {

    @Mock
    private AuditRepository auditRepository;
    @Mock
    private PerformanceRepository performanceRepository;

    @InjectMocks
    private AdminService adminService;

    @Test
    @DisplayName("Successfully get's all the system logs")
    void successfullyGetsAllTheSystemLogs(){

        when(auditRepository.findAll(any(PageRequest.class))).thenReturn(Page.empty());

        adminService.getSystemLogs(0,20);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);


        verify(auditRepository).findAll(captor.capture());

        Pageable pageable = captor.getValue();


        assertEquals(0,pageable.getPageNumber());
        assertEquals(20,pageable.getPageSize());
        assertEquals(Sort.by(Sort.Direction.DESC,"now"),pageable.getSort());
    }

    @Test
    @DisplayName("Successfully get's all the system performance data")
    void successfullyGetsAllTheSystemPerformanceData(){

        when(performanceRepository.findAll(any(PageRequest.class))).thenReturn((Page.empty()));

        adminService.getSystemPerformanceData(0,20);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

        verify(performanceRepository).findAll(captor.capture());

        Pageable pageable = captor.getValue();

        assertEquals(0,pageable.getPageNumber());
        assertEquals(20,pageable.getPageSize());
        assertEquals(Sort.by(Sort.Direction.DESC,"createdAt"),pageable.getSort());
    }
}