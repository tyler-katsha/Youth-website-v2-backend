package com.tyler.YouthEngedi.configurations;

import ai.onnxruntime.OrtEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OnnxConfiguration {

    @Bean
    public OrtEnvironment ortEnvironment(){
        return OrtEnvironment.getEnvironment();
    }
}
