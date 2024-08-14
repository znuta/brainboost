package com.brainboost.brainboost.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

public class NotFoundException extends RuntimeException {

    public static final String NOT_FOUND = "Not found";

    public NotFoundException() {
        super(NOT_FOUND, new HttpStatusCodeException(HttpStatus.NOT_FOUND) {
        });
    }
}

