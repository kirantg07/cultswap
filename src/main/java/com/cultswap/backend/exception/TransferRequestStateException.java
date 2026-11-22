package com.cultswap.backend.exception;

public class TransferRequestStateException extends RuntimeException {

    public TransferRequestStateException(String message) {
        super(message);
    }
}