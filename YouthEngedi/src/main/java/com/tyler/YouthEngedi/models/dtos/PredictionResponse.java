package com.tyler.YouthEngedi.models.dtos;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PredictionResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<Object> detections;
    private boolean approved;
}
