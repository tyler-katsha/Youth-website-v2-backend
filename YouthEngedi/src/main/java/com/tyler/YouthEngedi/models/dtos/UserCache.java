package com.tyler.YouthEngedi.models.dtos;

import com.tyler.YouthEngedi.models.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserCache {
    private long id;
    private String email;
    private String name;
    private String dateOfBirth;
    private String bio;
    private String profileImageUrl;
    private Set<Role> roles;
    private boolean isDeleted;
}
