package com.tyler.YouthEngedi.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR,reason = "Jwt token unable to generate this by the system")
public class NoJwtTokenGeneratedException extends RuntimeException {
    public NoJwtTokenGeneratedException(String message) {
        super(message);
    }
}
