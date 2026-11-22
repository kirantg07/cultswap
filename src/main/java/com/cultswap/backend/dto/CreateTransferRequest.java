package com.cultswap.backend.dto;

import jakarta.validation.constraints.Size;

public class CreateTransferRequest {

    @Size(max = 1000)
    private String message;

    public CreateTransferRequest() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}