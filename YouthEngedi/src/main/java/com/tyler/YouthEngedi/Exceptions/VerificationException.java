package com.tyler.YouthEngedi.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE,reason = "Cannot verify external OAuth 2.0 provider")
public class VerificationException extends RuntimeException {
    public VerificationException(String message) {
        super(message);
    }
}
