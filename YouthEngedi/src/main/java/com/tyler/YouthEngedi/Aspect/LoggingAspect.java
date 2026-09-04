package com.tyler.YouthEngedi.Aspect;

import com.tyler.YouthEngedi.Repository.PerformanceRepository;
import com.tyler.YouthEngedi.annotations.LogExecutionTime;
import com.tyler.YouthEngedi.models.Performance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
public class LoggingAspect {
    private final Logger logger = LogManager.getLogger(LoggingAspect.class);

    @Autowired
    private PerformanceRepository performanceRepository;

    @Around("@annotation(logExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint, LogExecutionTime logExecutionTime) throws Throwable {
        long start = System.currentTimeMillis();

        Object proceed = joinPoint.proceed();

        long executionTime = System.currentTimeMillis() - start;

        String description = logExecutionTime.value();

        String methodName = joinPoint.getSignature().toShortString();

        logger.info("{} - {} executed in {} ms", description, methodName, executionTime);

        String performanceDetails = String.format("%s - %s executed in %s ms", description, methodName, executionTime);

        boolean doSave = logExecutionTime.doSave();

        if(doSave){
            savePerformanceMetric(performanceDetails,description, methodName, executionTime);
        }


        return proceed;
    }

    @Async
    public void savePerformanceMetric(String performanceDetails,String description,String methodName,long executionTime){
        performanceRepository.save(Performance.builder().performanceDetails(performanceDetails).description(description).methodName(methodName).executionTime(executionTime).createdAt(LocalDateTime.now()).build());
    }
}
