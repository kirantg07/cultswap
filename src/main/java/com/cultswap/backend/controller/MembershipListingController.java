package com.cultswap.backend.controller;

import com.cultswap.backend.dto.*;
import com.cultswap.backend.entity.TransferRequest;
import com.cultswap.backend.service.MembershipListingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/listings")
@Validated
public class MembershipListingController {

    private final MembershipListingService membershipListingService;

    public MembershipListingController(
            MembershipListingService membershipListingService
    ) {
        this.membershipListingService = membershipListingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateListingResponse createListing(
            @Valid @RequestBody CreateListingRequest request,
            @AuthenticationPrincipal Long sellerId
    ) {
        return membershipListingService.createListing(request, sellerId);
    }

    @GetMapping
    public PaginatedResponse<ListingResponse> getActiveListings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        return membershipListingService.getActiveListings(pageable);
    }

    @GetMapping("/{listingId}")
    public ListingResponse getListingById(@PathVariable Long listingId, @AuthenticationPrincipal Long userId) {
        return membershipListingService.getListingById(listingId, userId);
    }

    @PutMapping("/{listingId}")
    public ListingResponse updateListing(
            @PathVariable Long listingId,
            @AuthenticationPrincipal Long sellerId,
            @Valid @RequestBody UpdateListingRequest request
    ) {
        return membershipListingService.updateListing(listingId, sellerId, request);
    }

    @DeleteMapping("/{listingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelListing(@PathVariable Long listingId, @AuthenticationPrincipal Long sellerId) {
        membershipListingService.cancelListing(listingId, sellerId);
    }

    @GetMapping("/my")
    public PaginatedResponse<ListingResponse> getMyListings(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10")  @Min(1) @Max(50) int size,
            @AuthenticationPrincipal Long sellerId
    ) {
        Pageable pageable = PageRequest.of(page, size);

        return membershipListingService.getMyListings(sellerId, pageable);
    }
}