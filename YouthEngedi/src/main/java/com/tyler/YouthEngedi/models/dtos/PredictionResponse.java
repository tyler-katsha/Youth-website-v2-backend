package com.tyler.YouthEngedi.models.dtos;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PredictionResponse {
    private List<Object> detections;
    private boolean approved;
}
