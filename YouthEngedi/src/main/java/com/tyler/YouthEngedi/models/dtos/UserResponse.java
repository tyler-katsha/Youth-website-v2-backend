package com.tyler.YouthEngedi.models.dtos;

import com.tyler.YouthEngedi.models.enums.AuthProvider;
import com.tyler.YouthEngedi.models.enums.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String name;
    private String dateOfBirth;
    private String email;
    private AuthProvider authProvider;
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;
    private String bio;
    private String profileImageUrl;
    private boolean enabled;
}
