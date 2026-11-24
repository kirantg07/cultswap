package com.cultswap.backend.service;

import com.cultswap.backend.dto.*;
import com.cultswap.backend.entity.MembershipListing;
import com.cultswap.backend.entity.TransferRequest;
import com.cultswap.backend.entity.User;
import com.cultswap.backend.exception.ListingNotFoundException;
import com.cultswap.backend.exception.ListingOwnershipException;
import com.cultswap.backend.exception.ListingStateException;
import com.cultswap.backend.exception.TransferRequestException;
import com.cultswap.backend.repository.MembershipListingRepository;
import com.cultswap.backend.repository.TransferRequestRepository;
import com.cultswap.backend.repository.UserRepository;
import com.cultswap.backend.service.eligibility.EligibilityResponse;
import com.cultswap.backend.service.eligibility.TransferEligibilityService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.util.List;

@Service
public class MembershipListingService {

    private final MembershipListingRepository membershipListingRepository;
    private final UserRepository userRepository;
    private final TransferRequestRepository transferRequestRepository;
    private final TransferEligibilityService transferEligibilityService;

    public MembershipListingService(
            MembershipListingRepository membershipListingRepository,
            UserRepository userRepository,
            TransferRequestRepository transferRequestRepository,
            TransferEligibilityService transferEligibilityService
    ) {
        this.membershipListingRepository = membershipListingRepository;
        this.userRepository = userRepository;
        this.transferRequestRepository = transferRequestRepository;
        this.transferEligibilityService = transferEligibilityService;
    }

    public User getSeller(Long sellerId) {
        return userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public CreateListingResponse createListing(CreateListingRequest request, Long sellerId) {

        User seller = getSeller(sellerId);

        MembershipListing listing = new MembershipListing();

        listing.setSeller(seller);
        listing.setMembershipType(request.getMembershipType());
        listing.setMembershipCategory(request.getMembershipCategory().name());
        listing.setMembershipDuration(request.getMembershipDuration().name());
        listing.setPurchaseDate(request.getPurchaseDate());
        listing.setExpiryDate(request.getExpiryDate());
        listing.setAskingPrice(request.getAskingPrice());
        listing.setCity(request.getCity());

        listing.setListingStatus("PENDING_VERIFICATION");
        listing.setDocumentVerificationStatus("PENDING");
        listing.setTransferUsed(false);

        EligibilityResponse eligibilityResponse =
                transferEligibilityService.evaluateEligibility(
                        request.getPurchaseDate(),
                        request.getMembershipDuration(),
                        request.getExpiryDate(),
                        listing.getTransferUsed(),
                        LocalDate.now()
                );

        listing.setTransferEligibilityStatus(
                eligibilityResponse.getStatus().name()
        );

        MembershipListing savedListing = membershipListingRepository.save(listing);

        return new CreateListingResponse(
                savedListing.getId(),
                savedListing.getMembershipType(),
                savedListing.getMembershipCategory(),
                savedListing.getMembershipDuration(),
                savedListing.getPurchaseDate(),
                savedListing.getExpiryDate(),
                savedListing.getAskingPrice(),
                savedListing.getCity(),
                savedListing.getListingStatus(),
                savedListing.getDocumentVerificationStatus(),
                savedListing.getTransferEligibilityStatus(),
                savedListing.getCreatedAt()
        );
    }

    public PaginatedResponse<ListingResponse> getActiveListings(Pageable pageable) {

        Page<MembershipListing> listings =
                membershipListingRepository.findByListingStatus("ACTIVE", pageable);

        List<ListingResponse> content = listings.getContent()
                .stream()
                .map(listing -> new ListingResponse(
                        listing.getId(),
                        listing.getMembershipType(),
                        listing.getExpiryDate(),
                        listing.getAskingPrice(),
                        listing.getCity()
                ))
                .toList();

        return new PaginatedResponse<>(
                content,
                listings.getNumber(),
                listings.getSize(),
                listings.getTotalElements(),
                listings.getTotalPages()
        );
    }

    public ListingResponse getListingById(Long listingId, Long userId) {

        MembershipListing listing = membershipListingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found"));

        boolean isOwner = listing.getSeller().getId().equals(userId);
        boolean isActive = "ACTIVE".equals(listing.getListingStatus());

        if (!isOwner && !isActive) {
            throw new ListingNotFoundException("Listing not found");
        }


        return new ListingResponse(
                listing.getId(),
                listing.getMembershipType(),
                listing.getExpiryDate(),
                listing.getAskingPrice(),
                listing.getCity()
        );
    }

    @Transactional
    public ListingResponse updateListing(
            Long listingId,
            Long sellerId,
            UpdateListingRequest request
    ) {
        MembershipListing listing = membershipListingRepository.findByIdWithLock(listingId)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found"));

        if (!listing.getSeller().getId().equals(sellerId)) {
            throw new ListingOwnershipException(
                    "You are not allowed to update this listing"
            );
        }

        if (!"ACTIVE".equals(listing.getListingStatus())) {
            throw new ListingStateException(
                    "Only active listings can be updated"
            );
        }

        listing.setMembershipType(request.getMembershipType());
        listing.setPurchaseDate(request.getPurchaseDate());
        listing.setExpiryDate(request.getExpiryDate());
        listing.setAskingPrice(request.getAskingPrice());
        listing.setCity(request.getCity());

        MembershipListing updatedListing = membershipListingRepository.save(listing);

        return new ListingResponse(
                updatedListing.getId(),
                updatedListing.getMembershipType(),
                updatedListing.getExpiryDate(),
                updatedListing.getAskingPrice(),
                updatedListing.getCity()
        );
    }

    @Transactional
    public void cancelListing(Long listingId, Long sellerId) {

        MembershipListing listing = membershipListingRepository.findByIdWithLock(listingId)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found"));

        if (!listing.getSeller().getId().equals(sellerId)) {
            throw new ListingOwnershipException(
                    "You are not allowed to cancel this listing"
            );
        }

        if ("SOLD".equals(listing.getListingStatus())
                || "TRANSFER_IN_PROGRESS".equals(listing.getListingStatus())
                || "CANCELLED".equals(listing.getListingStatus())) {

            throw new ListingStateException(
                    "This listing cannot be cancelled in its current status"
            );
        }

        listing.setListingStatus("CANCELLED");

        membershipListingRepository.save(listing);
    }

    public PaginatedResponse<ListingResponse> getMyListings(
            Long sellerId,
            Pageable pageable
    ) {
        Page<MembershipListing> listings =
                membershipListingRepository.findBySellerId(sellerId, pageable);

        List<ListingResponse> content = listings.getContent()
                .stream()
                .map(listing -> new ListingResponse(
                        listing.getId(),
                        listing.getMembershipType(),
                        listing.getExpiryDate(),
                        listing.getAskingPrice(),
                        listing.getCity()
                ))
                .toList();

        return new PaginatedResponse<>(
                content,
                listings.getNumber(),
                listings.getSize(),
                listings.getTotalElements(),
                listings.getTotalPages()
        );
    }
}