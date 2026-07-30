package com.tyler.YouthEngedi.models.mappers;

import com.tyler.YouthEngedi.models.RoleRequest;
import com.tyler.YouthEngedi.models.dtos.RoleChangeRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel="spring")
public interface RoleRequestMapper {


    RoleChangeRequest mapToRoleChangeRequest(RoleRequest request);
}
