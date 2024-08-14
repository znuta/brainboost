package com.brainboost.brainboost.exception;

public class DuplicateObjectException extends RuntimeException {

    public DuplicateObjectException(){super("The target object already exists");}

    public DuplicateObjectException(String message){super(message);}
}
