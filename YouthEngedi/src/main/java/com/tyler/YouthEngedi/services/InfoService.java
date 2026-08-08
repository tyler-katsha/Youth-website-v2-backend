package com.tyler.YouthEngedi.services;

import io.swagger.v3.oas.models.info.Info;
import org.springframework.stereotype.Service;

@Service
public class InfoService {
    public static final Info infoDetails = new Info().title("Youth Engedi API Documentation ").version("1.0").description("Comprehensive technical documentation for our platform's REST services");
}
