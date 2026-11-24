package com.cultswap.backend.service.eligibility;

public enum MembershipDuration {

    THREE_MONTHS(3, 15),
    SIX_MONTHS(6, 45),
    NINE_MONTHS(9, 90),
    TEN_MONTHS(10, 90),
    TWELVE_MONTHS(12, 90),
    TWENTY_FOUR_MONTHS(24, 120);

    private final int months;
    private final int allowedTransferDays;

    MembershipDuration(int months, int allowedTransferDays) {
        this.months = months;
        this.allowedTransferDays = allowedTransferDays;
    }

    public int getMonths() {
        return months;
    }

    public int getAllowedTransferDays() {
        return allowedTransferDays;
    }
}