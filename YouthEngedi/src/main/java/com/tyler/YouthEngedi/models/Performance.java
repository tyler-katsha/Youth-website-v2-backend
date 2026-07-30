package com.tyler.YouthEngedi.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Performance implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long performanceId;
    private String description;
    private String performanceDetails;
    private String methodName;
    private Long executionTime;
    private LocalDateTime createdAt;
}
