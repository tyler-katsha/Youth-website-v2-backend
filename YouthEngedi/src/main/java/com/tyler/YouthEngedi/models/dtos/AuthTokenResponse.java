package com.tyler.YouthEngedi.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthTokenResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String accessToken;
    private String refreshToken;
    private String familyId;
}
