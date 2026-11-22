package com.cultswap.backend.dto;

import java.time.LocalDate;

public interface ListingDateFields {

    LocalDate getPurchaseDate();

    LocalDate getExpiryDate();
}