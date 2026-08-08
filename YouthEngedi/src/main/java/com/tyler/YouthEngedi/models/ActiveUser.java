package com.tyler.YouthEngedi.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Month;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActiveUser {

    @Id
    private Month month;
    private int activeTotal;
}
