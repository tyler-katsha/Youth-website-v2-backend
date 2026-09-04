package com.tyler.YouthEngedi.models.mappers;

import com.tyler.YouthEngedi.models.User;
import com.tyler.YouthEngedi.models.dtos.Guest;
import com.tyler.YouthEngedi.models.dtos.UserProfileResponse;
import com.tyler.YouthEngedi.models.dtos.UserRegisterRequest;
import com.tyler.YouthEngedi.models.dtos.UserResponse;
import com.tyler.YouthEngedi.models.enums.AuthProvider;
import com.tyler.YouthEngedi.models.enums.Role;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;


@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User toUser(UserRegisterRequest request) {
        if (request == null) {
            return null;
        }

        return User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .dateOfBirth(request.getDateOfBirth())
                .password(request.getPassword())
                .authProvider(AuthProvider.LOCAL)
                .roles(Set.of(Role.MEMBER))
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Override
    public UserResponse mapToResponse(User existingUser) {
        if (existingUser == null) {
            return null;
        }

        return UserResponse
                .builder()
                .name(existingUser.getName())
                .dateOfBirth(existingUser.getDateOfBirth())
                .email(existingUser.getEmail())
                .enabled(existingUser.isEnabled())
                .authProvider(existingUser.getAuthProvider())
                .roles(existingUser.getRoles())
                .bio(existingUser.getBio())
                .profileImageUrl(existingUser.getProfileImageUrl())
                .build();
    }

    @Override
    public UserProfileResponse mapToProfileResponse(User existingUser) {
        return UserProfileResponse
                .builder()
                .bio(existingUser.getBio())
                .previewUrl(existingUser.getProfileImageUrl())
                .build();
    }

    @Override
    public Guest toGuest(User guestUser) {
        return Guest.builder()
                .fakeUserId(guestUser.getId())
                .fakeEmail(guestUser.getEmail())
                .createdAt(Instant.now().toEpochMilli())
                .build();
    }
}
