package com.cultswap.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ListingResponse {

    private Long id;
    private String membershipType;
    private LocalDate expiryDate;
    private BigDecimal askingPrice;
    private String city;

    public ListingResponse(
            Long id,
            String membershipType,
            LocalDate expiryDate,
            BigDecimal askingPrice,
            String city
    ) {
        this.id = id;
        this.membershipType = membershipType;
        this.expiryDate = expiryDate;
        this.askingPrice = askingPrice;
        this.city = city;
    }

    public Long getId() {
        return id;
    }

    public String getMembershipType() {
        return membershipType;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public BigDecimal getAskingPrice() {
        return askingPrice;
    }

    public String getCity() {
        return city;
    }
}