package com.tyler.YouthEngedi.models;

import com.tyler.YouthEngedi.models.enums.ConnectionType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminEvent {

    @Enumerated(EnumType.STRING)
    private ConnectionType type;   // CONNECT, DISCONNECT, REQUEST, TRAFFIC, ERROR
    @Id
    private String userId;
    private String message;
    private long timestamp;
}
