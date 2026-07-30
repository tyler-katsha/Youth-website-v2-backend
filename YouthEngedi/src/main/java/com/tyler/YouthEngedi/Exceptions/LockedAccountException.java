package com.tyler.YouthEngedi.Exceptions;

public class LockedAccountException extends RuntimeException {
    public LockedAccountException(String message) {
        super(message);
    }
}
