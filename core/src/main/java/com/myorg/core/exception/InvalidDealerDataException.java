package com.myorg.core.exception;

public class InvalidDealerDataException extends Exception {

    private static final long serialVersionUID = 1L;

    public InvalidDealerDataException(String message) {
        super(message);
    }
}
