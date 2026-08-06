package com.tyler.YouthEngedi.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FOUND,reason = "Role request is still Pending.")
public class RoleRequestPendingException extends RuntimeException {
    public RoleRequestPendingException(String message) {
        super(message);
    }
}
