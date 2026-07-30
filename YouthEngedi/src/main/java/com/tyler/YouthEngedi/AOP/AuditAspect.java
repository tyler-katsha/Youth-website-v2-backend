package com.tyler.YouthEngedi.AOP;

import com.tyler.YouthEngedi.Repository.AuditRepository;
import com.tyler.YouthEngedi.annotations.AuditAction;
import com.tyler.YouthEngedi.models.AuditLog;
import com.tyler.YouthEngedi.models.enums.AuditStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
public class AuditAspect {
    private final Logger logger = LogManager.getLogger(AuditAspect.class);

    @Autowired
    private AuditRepository auditRepository;

    @AfterReturning(pointcut = "@annotation(auditAction)",returning = "result")
    public void auditSuccess(AuditAction auditAction, Object result){

        String username = extractUsername();

        AuditLog log = AuditLog
                .builder()
                .now(LocalDateTime.now())
                .value(auditAction.value())
                .status(AuditStatus.SUCCESSFUL)
                .performedBy(username)
                .build();
        auditRepository.save(log);
    }

    @AfterThrowing(pointcut = "@annotation(auditAction)",throwing = "exception")
    public void auditFailure(AuditAction auditAction, Exception exception){

        String username = extractUsername();

        AuditLog log = AuditLog
                .builder()
                .now(LocalDateTime.now())
                .value(auditAction.value() + " - FAILED: " + exception.getMessage())
                .status(AuditStatus.FAILED)
                .performedBy(username)
                .build();
        auditRepository.save(log);
    }

    private String extractUsername() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null || !authentication.isAuthenticated()){
            return "SYSTEM_OR_ANONYMOUS";
        }

        Object principal = authentication.getPrincipal();

        if(principal instanceof Jwt jwt){
            return jwt.hasClaim("email") ? jwt.getClaimAsString("email") : jwt.getSubject();
        } else if(principal instanceof OAuth2User oAuth2User){
            return oAuth2User.getAttribute("email");
        }

        return authentication.getName();
    }
}
