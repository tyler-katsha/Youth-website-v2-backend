package com.tyler.YouthEngedi.models.dtos;

import com.tyler.YouthEngedi.models.enums.ImageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImageRequest {

    private ImageStatus status;
    private boolean isSoftDelete;
}
