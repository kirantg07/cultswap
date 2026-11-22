package com.cultswap.backend.dto;

import java.time.OffsetDateTime;

public class TransferRequestResponse {

    private Long id;
    private Long listingId;
    private Long buyerId;
    private String status;
    private String message;
    private OffsetDateTime requestedAt;

    public TransferRequestResponse(
            Long id,
            Long listingId,
            Long buyerId,
            String status,
            String message,
            OffsetDateTime requestedAt
    ) {
        this.id = id;
        this.listingId = listingId;
        this.buyerId = buyerId;
        this.status = status;
        this.message = message;
        this.requestedAt = requestedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getListingId() {
        return listingId;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public OffsetDateTime getRequestedAt() {
        return requestedAt;
    }
}