package com.cultswap.backend.exception;

public class TransferRequestException extends RuntimeException {

    public TransferRequestException(String message) {
        super(message);
    }
}