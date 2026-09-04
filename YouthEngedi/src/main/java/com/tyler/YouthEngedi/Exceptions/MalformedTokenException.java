package com.tyler.YouthEngedi.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_CONTENT,reason = "Malformed Token")
public class MalformedTokenException extends RuntimeException {
    public MalformedTokenException(String message) {
        super(message);
    }
}
