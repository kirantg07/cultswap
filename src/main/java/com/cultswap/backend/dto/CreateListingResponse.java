package com.cultswap.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public class CreateListingResponse {

    public CreateListingResponse(
            Long id,
            String membershipType,
            LocalDate purchaseDate,
            LocalDate expiryDate,
            BigDecimal askingPrice,
            String city,
            String listingStatus,
            String documentVerificationStatus,
            String transferEligibilityStatus,
            OffsetDateTime createdAt) {

        this.id = id;
        this.membershipType = membershipType;
        this.purchaseDate = purchaseDate;
        this.expiryDate = expiryDate;
        this.askingPrice = askingPrice;
        this.city = city;
        this.listingStatus = listingStatus;
        this.documentVerificationStatus = documentVerificationStatus;
        this.transferEligibilityStatus = transferEligibilityStatus;
        this.createdAt = createdAt;
    }

    private Long id;
    private String membershipType;
    private LocalDate purchaseDate;
    private LocalDate expiryDate;
    private BigDecimal askingPrice;
    private String city;
    private String listingStatus;
    private String documentVerificationStatus;
    private String transferEligibilityStatus;
    private OffsetDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMembershipType() {
        return membershipType;
    }

    public void setMembershipType(String membershipType) {
        this.membershipType = membershipType;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDate purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public BigDecimal getAskingPrice() {
        return askingPrice;
    }

    public void setAskingPrice(BigDecimal askingPrice) {
        this.askingPrice = askingPrice;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getListingStatus() {
        return listingStatus;
    }

    public void setListingStatus(String listingStatus) {
        this.listingStatus = listingStatus;
    }

    public String getDocumentVerificationStatus() {
        return documentVerificationStatus;
    }

    public void setDocumentVerificationStatus(String documentVerificationStatus) {
        this.documentVerificationStatus = documentVerificationStatus;
    }

    public String getTransferEligibilityStatus() {
        return transferEligibilityStatus;
    }

    public void setTransferEligibilityStatus(String transferEligibilityStatus) {
        this.transferEligibilityStatus = transferEligibilityStatus;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}