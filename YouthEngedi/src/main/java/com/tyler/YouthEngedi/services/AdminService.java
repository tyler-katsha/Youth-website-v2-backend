package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Exceptions.ResourceNotFoundException;
import com.tyler.YouthEngedi.Repository.AuditRepository;
import com.tyler.YouthEngedi.Repository.PerformanceRepository;
import com.tyler.YouthEngedi.Repository.UserRepository;
import com.tyler.YouthEngedi.models.AuditLog;
import com.tyler.YouthEngedi.models.Performance;
import com.tyler.YouthEngedi.models.User;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final AuditRepository auditRepository;
    private final PerformanceRepository performanceRepository;

    public AdminService(AuditRepository auditRepository,PerformanceRepository performanceRepository){
        this.auditRepository = auditRepository;
        this.performanceRepository = performanceRepository;
    }
    public Page<AuditLog> getSystemLogs(int page, int size){
        return auditRepository.findAll(PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,"now")));
    }

    public Page<Performance> getSystemPerformanceData(int page, int size){

        return performanceRepository.findAll(PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,"createdAt")));
    }
//
//    public void sendTestEmail(String type) {
//        emailService.sendTestEmail(type);
//    }
}
