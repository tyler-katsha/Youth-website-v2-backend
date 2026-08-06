package com.tyler.YouthEngedi.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST,reason = "Event was not found")
public class EventException extends RuntimeException {
    public EventException(String message) {
        super(message);
    }
}
