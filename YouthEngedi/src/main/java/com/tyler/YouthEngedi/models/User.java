package com.tyler.YouthEngedi.models;

import com.tyler.YouthEngedi.models.enums.AuthProvider;
import com.tyler.YouthEngedi.models.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minidev.json.annotate.JsonIgnore;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "UPDATE user SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
@Schema(description = "Details about all user information and attributes of a fully implemented")
public class User implements UserDetails, Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique ID for a single user.", example = "1")
    private long id;
    @Schema(description = "Name of a user.",example = "John Doe")
    private String name;
    @Schema(description = "Email of a user.",example = "johnDoe12@gmail.com")
    @Column(nullable = false, unique = true)
    private String email;
    @Schema(description = "Date of birth of user",example = "2025/01/01")
    private String dateOfBirth;
    @Schema(description = "password of user.",example = "pass123")
    @JsonIgnore
    private String password;
    @Schema(description = "Bio of user",example = "Hey, Guys nice to meet you all.")
    private String bio;
    @Schema(description = "URL for user image stored in the cloud",example = "path/to/image.png")
    private String profileImageUrl;
    @Schema(description = "Provider of how the user login to the app",example = "Local or OAuth based")
    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider;
    @Schema(description = "Set of user roles that gives them different levels of access to the application and features",example = "['MEMBER','YOUTH_LEADER','ADMIN']")
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;
    @Schema(description = "Status if a user is allowed to enter the application if they are, they can enter if not an verification email is sent to them",example = "enabled = 1, not enabled = 0")
    private boolean enabled;
    @CreationTimestamp
    @Schema(description = "The date and time when there account is created for the first time they registered there account.",example = "2027/12/01")
    private LocalDateTime createdAt;
    @Column(nullable = false)
    @Schema(description = "Soft delete there account from the db not permanently deleting there account so there details is still recoverable.",example = "deleted = 1, soft delete = 0")
    private boolean isDeleted = false;

    @Override
    @NullMarked
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.name())).collect(Collectors.toSet());
    }

    @Override
    @NullMarked
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
