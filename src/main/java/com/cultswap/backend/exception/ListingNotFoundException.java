package com.cultswap.backend.exception;

public class ListingNotFoundException extends RuntimeException {

    public ListingNotFoundException(String message) {
        super(message);
    }
}