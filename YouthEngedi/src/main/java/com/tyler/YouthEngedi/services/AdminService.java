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
@RequiredArgsConstructor
public class AdminService {

    private final static Logger logger = LogManager.getLogger(AdminService.class);

    private final AuditRepository auditRepository;
    private final PerformanceRepository performanceRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public ResponseEntity<Page<AuditLog>> getSystemLogs(int page, int size){
        return ResponseEntity.ok(auditRepository.findAll(PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,"now"))));
    }

    public ResponseEntity<Page<Performance>> getSystemPerformanceData(int page, int size){

        return ResponseEntity.ok(performanceRepository.findAll(PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,"createdAt"))));
    }

    public boolean deactivateUser(String email) {

        try{
            User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User doesn't exist"));

            user.setDeleted(true);

            return true;
        } catch (Exception e){
            return false;
        }
    }

    public boolean activateUser(String email) {

        try {
            User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User doesn't exist"));

            user.setDeleted(false);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void sendTestEmail(String type) {
        try{
            emailService.sendTestEmail(type);
        } catch (MessagingException e){
            logger.error("Failed to send email to {}","me",e);
        }
    }
}
