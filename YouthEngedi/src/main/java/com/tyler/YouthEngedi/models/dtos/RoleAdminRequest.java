package com.tyler.YouthEngedi.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleAdminRequest {
    private long roleReqId;
    private boolean wasReviewed;
    private long userId;
    private String requestedRole;
    private String email;
    private String adminComment;
    private long review_by;
}
