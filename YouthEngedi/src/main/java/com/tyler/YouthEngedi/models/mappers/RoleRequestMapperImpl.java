package com.tyler.YouthEngedi.models.mappers;

import com.tyler.YouthEngedi.models.RoleRequest;
import com.tyler.YouthEngedi.models.dtos.RoleChangeRequest;
import org.springframework.stereotype.Component;

@Component
public class RoleRequestMapperImpl implements RoleRequestMapper{
    @Override
    public RoleChangeRequest mapToRoleChangeRequest(RoleRequest request) {
        return RoleChangeRequest
                .builder()
                .roleReqId(request.getRoleReqId())
                .build();
    }
}
