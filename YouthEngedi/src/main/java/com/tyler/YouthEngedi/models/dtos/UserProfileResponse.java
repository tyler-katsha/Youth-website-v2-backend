package com.tyler.YouthEngedi.models.dtos;

import com.tyler.YouthEngedi.models.enums.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileResponse {
    private String name;
    private String dateOfBirth;
    private String bio;
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;
}
