package com.cultswap.backend.service.eligibility;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransferEligibilityService {

    public EligibilityResult checkTransferWindow(
            LocalDate purchaseDate,
            int allowedTransferDays,
            LocalDate today) {

        if (purchaseDate == null || today == null) {
            return EligibilityResult.NEEDS_REVIEW;
        }

        if (purchaseDate.isAfter(today)) {
            return EligibilityResult.NEEDS_REVIEW;
        }

        long daysSincePurchase =
                ChronoUnit.DAYS.between(purchaseDate, today);

        if (daysSincePurchase <= allowedTransferDays) {
            return EligibilityResult.ELIGIBLE;
        }

        return EligibilityResult.INELIGIBLE;
    }

    public EligibilityResult checkTransferWindow(
            LocalDate purchaseDate,
            MembershipDuration duration,
            LocalDate today) {

        if (duration == null) {
            return EligibilityResult.NEEDS_REVIEW;
        }

        return checkTransferWindow(
                purchaseDate,
                duration.getAllowedTransferDays(),
                today
        );
    }

    public EligibilityResult checkPreviousTransfer(Boolean transferUsed) {

        if (transferUsed == null) {
            return EligibilityResult.NEEDS_REVIEW;
        }

        if (transferUsed) {
            return EligibilityResult.INELIGIBLE;
        }

        return EligibilityResult.ELIGIBLE;
    }

    public EligibilityResult checkMembershipExpiry(
            LocalDate expiryDate,
            LocalDate today) {

        if (expiryDate == null || today == null) {
            return EligibilityResult.NEEDS_REVIEW;
        }

        if (expiryDate.isBefore(today)) {
            return EligibilityResult.INELIGIBLE;
        }

        return EligibilityResult.ELIGIBLE;
    }

    public EligibilityResult checkOverallEligibility(
            LocalDate purchaseDate,
            MembershipDuration duration,
            LocalDate expiryDate,
            Boolean transferUsed,
            LocalDate today) {

        EligibilityResult transferWindowResult =
                checkTransferWindow(purchaseDate, duration, today);

        EligibilityResult previousTransferResult =
                checkPreviousTransfer(transferUsed);

        EligibilityResult expiryResult =
                checkMembershipExpiry(expiryDate, today);

        // Any failed condition makes the membership ineligible.
        if (transferWindowResult == EligibilityResult.INELIGIBLE
                || previousTransferResult == EligibilityResult.INELIGIBLE
                || expiryResult == EligibilityResult.INELIGIBLE) {
            return EligibilityResult.INELIGIBLE;
        }

        // If any condition is unknown, manual review is required.
        if (transferWindowResult == EligibilityResult.NEEDS_REVIEW
                || previousTransferResult == EligibilityResult.NEEDS_REVIEW
                || expiryResult == EligibilityResult.NEEDS_REVIEW) {
            return EligibilityResult.NEEDS_REVIEW;
        }

        return EligibilityResult.ELIGIBLE;
    }

    public EligibilityResponse evaluateEligibility(
            LocalDate purchaseDate,
            MembershipDuration duration,
            LocalDate expiryDate,
            Boolean transferUsed,
            LocalDate today) {

        List<String> reasons = new ArrayList<>();

        EligibilityResult transferWindowResult =
                checkTransferWindow(purchaseDate, duration, today);

        EligibilityResult previousTransferResult =
                checkPreviousTransfer(transferUsed);

        EligibilityResult expiryResult =
                checkMembershipExpiry(expiryDate, today);

        if (transferWindowResult == EligibilityResult.INELIGIBLE) {
            reasons.add("TRANSFER_WINDOW_EXPIRED");
        }

        if (previousTransferResult == EligibilityResult.INELIGIBLE) {
            reasons.add("MEMBERSHIP_ALREADY_TRANSFERRED");
        }

        if (expiryResult == EligibilityResult.INELIGIBLE) {
            reasons.add("MEMBERSHIP_EXPIRED");
        }

        if (transferWindowResult == EligibilityResult.INELIGIBLE
                || previousTransferResult == EligibilityResult.INELIGIBLE
                || expiryResult == EligibilityResult.INELIGIBLE) {
            return new EligibilityResponse(
                    EligibilityResult.INELIGIBLE, reasons);
        }

        if (transferWindowResult == EligibilityResult.NEEDS_REVIEW) {
            reasons.add("TRANSFER_WINDOW_CANNOT_BE_VERIFIED");
        }

        if (previousTransferResult == EligibilityResult.NEEDS_REVIEW) {
            reasons.add("TRANSFER_STATUS_UNKNOWN");
        }

        if (expiryResult == EligibilityResult.NEEDS_REVIEW) {
            reasons.add("EXPIRY_DATE_UNKNOWN");
        }

        if (!reasons.isEmpty()) {
            return new EligibilityResponse(
                    EligibilityResult.NEEDS_REVIEW, reasons);
        }

        return new EligibilityResponse(
                EligibilityResult.ELIGIBLE, reasons);
    }
}