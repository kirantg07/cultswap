package com.cultswap.backend.service.eligibility;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransferEligibilityServiceTest {

    private final TransferEligibilityService service =
            new TransferEligibilityService();

    @Test
    void shouldBeEligibleWithinTransferWindow() {

        LocalDate purchaseDate = LocalDate.of(2026, 11, 1);
        LocalDate today = LocalDate.of(2026, 11, 10);

        EligibilityResult result =
                service.checkTransferWindow(purchaseDate, 15, today);

        assertEquals(EligibilityResult.ELIGIBLE, result);
    }

    @Test
    void shouldBeIneligibleOutsideTransferWindow() {

        LocalDate purchaseDate = LocalDate.of(2026, 11, 1);
        LocalDate today = LocalDate.of(2026, 11, 20);

        EligibilityResult result =
                service.checkTransferWindow(purchaseDate, 15, today);

        assertEquals(EligibilityResult.INELIGIBLE, result);
    }

    @Test
    void shouldNeedReviewWhenPurchaseDateIsMissing() {

        LocalDate today = LocalDate.of(2026, 11, 20);

        EligibilityResult result =
                service.checkTransferWindow(null, 15, today);

        assertEquals(EligibilityResult.NEEDS_REVIEW, result);
    }

    @Test
    void shouldBeEligibleForThreeMonthMembershipWithinFifteenDays() {

        LocalDate purchaseDate = LocalDate.of(2026, 11, 1);
        LocalDate today = LocalDate.of(2026, 11, 16);

        EligibilityResult result = service.checkTransferWindow(
                purchaseDate,
                MembershipDuration.THREE_MONTHS,
                today
        );

        assertEquals(EligibilityResult.ELIGIBLE, result);
    }

    @Test
    void shouldBeIneligibleForThreeMonthMembershipAfterFifteenDays() {

        LocalDate purchaseDate = LocalDate.of(2026, 11, 1);
        LocalDate today = LocalDate.of(2026, 11, 17);

        EligibilityResult result = service.checkTransferWindow(
                purchaseDate,
                MembershipDuration.THREE_MONTHS,
                today
        );

        assertEquals(EligibilityResult.INELIGIBLE, result);
    }

    @Test
    void shouldBeEligibleForTwentyFourMonthMembershipWithinTransferWindow() {

        LocalDate purchaseDate = LocalDate.of(2026, 11, 1);
        LocalDate today = LocalDate.of(2027, 2, 1);

        EligibilityResult result = service.checkTransferWindow(
                purchaseDate,
                MembershipDuration.TWENTY_FOUR_MONTHS,
                today
        );

        assertEquals(EligibilityResult.ELIGIBLE, result);
    }

    @Test
    void shouldBeIneligibleWhenMembershipWasAlreadyTransferred() {
        EligibilityResult result = service.checkPreviousTransfer(true);

        assertEquals(EligibilityResult.INELIGIBLE, result);
    }

    @Test
    void shouldBeEligibleWhenMembershipWasNotTransferred() {
        EligibilityResult result = service.checkPreviousTransfer(false);

        assertEquals(EligibilityResult.ELIGIBLE, result);
    }

    @Test
    void shouldNeedReviewWhenTransferStatusIsUnknown() {
        EligibilityResult result = service.checkPreviousTransfer(null);

        assertEquals(EligibilityResult.NEEDS_REVIEW, result);
    }

    @Test
    void shouldBeIneligibleWhenMembershipHasExpired() {
        LocalDate expiryDate = LocalDate.of(2026, 11, 22);
        LocalDate today = LocalDate.of(2026, 11, 23);

        EligibilityResult result =
                service.checkMembershipExpiry(expiryDate, today);

        assertEquals(EligibilityResult.INELIGIBLE, result);
    }

    @Test
    void shouldBeEligibleWhenMembershipExpiresToday() {
        LocalDate expiryDate = LocalDate.of(2026, 11, 23);
        LocalDate today = LocalDate.of(2026, 11, 23);

        EligibilityResult result =
                service.checkMembershipExpiry(expiryDate, today);

        assertEquals(EligibilityResult.ELIGIBLE, result);
    }

    @Test
    void shouldBeEligibleWhenMembershipExpiresInFuture() {
        LocalDate expiryDate = LocalDate.of(2026, 12, 23);
        LocalDate today = LocalDate.of(2026, 11, 23);

        EligibilityResult result =
                service.checkMembershipExpiry(expiryDate, today);

        assertEquals(EligibilityResult.ELIGIBLE, result);
    }

    @Test
    void shouldNeedReviewWhenExpiryDateIsMissing() {
        EligibilityResult result =
                service.checkMembershipExpiry(null, LocalDate.of(2026, 11, 23));

        assertEquals(EligibilityResult.NEEDS_REVIEW, result);
    }

    @Test
    void shouldBeEligibleWhenAllConditionsAreSatisfied() {
        EligibilityResult result = service.checkOverallEligibility(
                LocalDate.of(2026, 11, 10),
                MembershipDuration.THREE_MONTHS,
                LocalDate.of(2027, 2, 10),
                false,
                LocalDate.of(2026, 11, 20)
        );

        assertEquals(EligibilityResult.ELIGIBLE, result);
    }

    @Test
    void shouldBeIneligibleWhenTransferWindowHasExpired() {
        EligibilityResult result = service.checkOverallEligibility(
                LocalDate.of(2026, 10, 1),
                MembershipDuration.THREE_MONTHS,
                LocalDate.of(2027, 2, 1),
                false,
                LocalDate.of(2026, 11, 20)
        );

        assertEquals(EligibilityResult.INELIGIBLE, result);
    }

    @Test
    void shouldReturnEligibleWithEmptyReasonsWhenAllConditionsPass() {
        EligibilityResponse response = service.evaluateEligibility(
                LocalDate.of(2026, 11, 10),
                MembershipDuration.THREE_MONTHS,
                LocalDate.of(2027, 2, 10),
                false,
                LocalDate.of(2026, 11, 20)
        );

        assertEquals(EligibilityResult.ELIGIBLE, response.getStatus());
        assertTrue(response.getReasons().isEmpty());
    }

    @Test
    void shouldReturnTransferWindowExpiredReason() {
        EligibilityResponse response = service.evaluateEligibility(
                LocalDate.of(2026, 10, 1),
                MembershipDuration.THREE_MONTHS,
                LocalDate.of(2027, 2, 1),
                false,
                LocalDate.of(2026, 11, 20)
        );

        assertEquals(EligibilityResult.INELIGIBLE, response.getStatus());
        assertEquals(
                List.of("TRANSFER_WINDOW_EXPIRED"),
                response.getReasons()
        );
    }

    @Test
    void shouldReturnMultipleReasonsWhenMultipleConditionsFail() {
        EligibilityResponse response = service.evaluateEligibility(
                LocalDate.of(2026, 10, 1),
                MembershipDuration.THREE_MONTHS,
                LocalDate.of(2026, 11, 15),
                true,
                LocalDate.of(2026, 11, 20)
        );

        assertEquals(EligibilityResult.INELIGIBLE, response.getStatus());

        assertEquals(
                List.of(
                        "TRANSFER_WINDOW_EXPIRED",
                        "MEMBERSHIP_ALREADY_TRANSFERRED",
                        "MEMBERSHIP_EXPIRED"
                ),
                response.getReasons()
        );
    }

    @Test
    void shouldReturnNeedsReviewWithReasonWhenTransferStatusIsUnknown() {
        EligibilityResponse response = service.evaluateEligibility(
                LocalDate.of(2026, 11, 10),
                MembershipDuration.THREE_MONTHS,
                LocalDate.of(2027, 2, 10),
                null,
                LocalDate.of(2026, 11, 20)
        );

        assertEquals(EligibilityResult.NEEDS_REVIEW, response.getStatus());
        assertEquals(
                List.of("TRANSFER_STATUS_UNKNOWN"),
                response.getReasons()
        );
    }
}