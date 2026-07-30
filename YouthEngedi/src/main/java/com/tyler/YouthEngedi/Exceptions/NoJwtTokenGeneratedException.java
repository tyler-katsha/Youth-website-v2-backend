package com.tyler.YouthEngedi.Exceptions;

public class NoJwtTokenGeneratedException extends RuntimeException {
    public NoJwtTokenGeneratedException(String message) {
        super(message);
    }
}
