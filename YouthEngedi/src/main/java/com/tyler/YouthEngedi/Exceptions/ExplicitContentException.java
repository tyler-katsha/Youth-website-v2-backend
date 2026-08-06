package com.tyler.YouthEngedi.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST,reason = "Explicit or adult content is not allowed")
public class ExplicitContentException extends RuntimeException {
    public ExplicitContentException(String message) {
        super(message);
    }
}
