package com.upwork.urlshortener.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InvalidCustomKeyException extends ResponseStatusException {
    public InvalidCustomKeyException(String msg) {
        super(HttpStatus.CONFLICT, msg);
    }
}
