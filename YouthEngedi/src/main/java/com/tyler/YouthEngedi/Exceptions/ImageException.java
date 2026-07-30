package com.tyler.YouthEngedi.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST,reason = "Unable to process image")
public class ImageException extends RuntimeException {
    public ImageException(String message) {
        super(message);
    }
}
