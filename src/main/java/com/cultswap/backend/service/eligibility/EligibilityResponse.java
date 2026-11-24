package com.cultswap.backend.service.eligibility;

import java.util.List;

public class EligibilityResponse {

    private final EligibilityResult status;
    private final List<String> reasons;

    public EligibilityResponse(
            EligibilityResult status,
            List<String> reasons) {
        this.status = status;
        this.reasons = reasons;
    }

    public EligibilityResult getStatus() {
        return status;
    }

    public List<String> getReasons() {
        return reasons;
    }
}