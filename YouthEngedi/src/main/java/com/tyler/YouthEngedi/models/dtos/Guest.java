package com.tyler.YouthEngedi.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Guest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long fakeUserId;
    private String fakeEmail;
    private long createdAt;

}
