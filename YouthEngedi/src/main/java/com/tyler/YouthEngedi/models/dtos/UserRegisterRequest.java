package com.tyler.YouthEngedi.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterRequest {
    private String name;
    private String email;
    private String dateOfBirth;
    private String password;
    private MultipartFile profileImageUrl;
}
