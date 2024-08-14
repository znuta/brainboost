package com.brainboost.brainboost.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class BadRequestException extends RuntimeException {

    public static final String BAD_REQUEST = "Not found";

    public BadRequestException() {
        super(BAD_REQUEST, new HttpStatusCodeException(HttpStatus.BAD_REQUEST) {
        });
    }

    public BadRequestException(String message) {
        super(message, new HttpStatusCodeException(HttpStatus.BAD_REQUEST) {
        });
    }
}

