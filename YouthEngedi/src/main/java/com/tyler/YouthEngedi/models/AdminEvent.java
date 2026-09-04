package com.tyler.YouthEngedi.models;

import com.fasterxml.jackson.annotation.JsonProperty;
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


    @Id
    private String email;
    private String message;
    private long timestamp;
    @Enumerated(EnumType.STRING)
    @JsonProperty("connectionType")
    private ConnectionType type;   // CONNECT, DISCONNECT, REQUEST, TRAFFIC, ERROR
}
