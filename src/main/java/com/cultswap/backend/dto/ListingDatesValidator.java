package com.cultswap.backend.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ListingDatesValidator
        implements ConstraintValidator<ValidListingDates, ListingDateFields> {

    @Override
    public boolean isValid(
            ListingDateFields request,
            ConstraintValidatorContext context) {

        if (request == null
                || request.getPurchaseDate() == null
                || request.getExpiryDate() == null) {
            return true;
        }

        return request.getExpiryDate()
                .isAfter(request.getPurchaseDate());
    }
}