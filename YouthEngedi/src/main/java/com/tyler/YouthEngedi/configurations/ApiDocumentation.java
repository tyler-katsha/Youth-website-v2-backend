package com.tyler.YouthEngedi.configurations;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiDocumentation {

    @Bean
    public OpenAPI openAPI(){
        return new OpenAPI().info(new Info().title("Youth Engedi API Documentation ").version("1.0").description("Comprehensive technical documentation for our platform's REST services"));
    }
}
