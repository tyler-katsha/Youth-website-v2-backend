package com.tyler.YouthEngedi.models.mappers;

import com.tyler.YouthEngedi.models.User;
import com.tyler.YouthEngedi.models.dtos.UserCache;
import com.tyler.YouthEngedi.models.dtos.UserProfileResponse;
import com.tyler.YouthEngedi.models.dtos.UserRegisterRequest;
import com.tyler.YouthEngedi.models.dtos.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel="spring")
public interface UserMapper {

    @Mapping(target= "name")
    @Mapping(target= "id",ignore = true)
    @Mapping(target = "roles",constant = "[MEMBER]")
    @Mapping(target = "profileImageUrl",ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target="bio",ignore = true)
    @Mapping(target = "authProvider",constant = "LOCAL")
    @Mapping(target="createdAt",ignore = true)
    @Mapping(target="updatedAt",ignore = true)
    @Mapping(target="enabled")
    @Mapping(target = "isDeleted",ignore = true)
    User toUser(UserRegisterRequest request);

    UserResponse mapToResponse(User existingUser);
    // UserResponse mapToResponse(UserCache existingCacheUser);

    UserProfileResponse mapToProfileResponse(User existingUser);
}
