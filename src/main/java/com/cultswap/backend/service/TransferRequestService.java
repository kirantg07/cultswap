package com.cultswap.backend.service;

import com.cultswap.backend.dto.TransferRequestResponse;
import com.cultswap.backend.entity.MembershipListing;
import com.cultswap.backend.entity.TransferRequest;
import com.cultswap.backend.entity.User;
import com.cultswap.backend.exception.*;
import com.cultswap.backend.repository.MembershipListingRepository;
import com.cultswap.backend.repository.TransferRequestRepository;
import com.cultswap.backend.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class TransferRequestService {

    private final TransferRequestRepository transferRequestRepository;
    private final MembershipListingRepository membershipListingRepository;
    private final UserRepository userRepository;

    public TransferRequestService(
            TransferRequestRepository transferRequestRepository,
            MembershipListingRepository membershipListingRepository,
            UserRepository userRepository
    ) {
        this.transferRequestRepository = transferRequestRepository;
        this.membershipListingRepository = membershipListingRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TransferRequestResponse createTransferRequest(
            Long listingId,
            Long buyerId,
            String message
    ) {
        MembershipListing listing = membershipListingRepository.findByIdWithLock(listingId)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found"));

        if (!"ACTIVE".equals(listing.getListingStatus())) {
            throw new TransferRequestException(
                    "Listing is not available for transfer requests"
            );
        }

        if (listing.getSeller().getId().equals(buyerId)) {
            throw new TransferRequestException(
                    "You cannot request your own listing"
            );
        }

        boolean alreadyPending =
                transferRequestRepository.existsByListingIdAndBuyerIdAndStatus(
                        listingId,
                        buyerId,
                        "PENDING"
                );

        if (alreadyPending) {
            throw new TransferRequestException(
                    "You already have a pending request for this listing"
            );
        }

        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new UserNotFoundException("Buyer not found"));

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setListing(listing);
        transferRequest.setBuyer(buyer);
        transferRequest.setStatus("PENDING");
        transferRequest.setMessage(message);

        TransferRequest savedRequest =
                transferRequestRepository.save(transferRequest);

        return new TransferRequestResponse(
                savedRequest.getId(),
                savedRequest.getListing().getId(),
                savedRequest.getBuyer().getId(),
                savedRequest.getStatus(),
                savedRequest.getMessage(),
                savedRequest.getRequestedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<TransferRequestResponse> getListingTransferRequests(
            Long listingId,
            Long sellerId
    ) {
        MembershipListing listing = membershipListingRepository.findById(listingId)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found"));

        // Ensure the logged-in user owns this listing
        if (!listing.getSeller().getId().equals(sellerId)) {
            throw new ListingOwnershipException(
                    "You are not authorized to view requests for this listing"
            );
        }

        List<TransferRequest> requests =
                transferRequestRepository.findByListingId(listingId);

        return requests.stream()
                .map(request -> new TransferRequestResponse(
                        request.getId(),
                        request.getListing().getId(),
                        request.getBuyer().getId(),
                        request.getStatus(),
                        request.getMessage(),
                        request.getRequestedAt()
                ))
                .toList();
    }

    @Transactional
    public TransferRequestResponse acceptTransferRequest(
            Long listingId,
            Long requestId,
            Long sellerId
    ) {
        // 1. Find the listing
        MembershipListing listing = membershipListingRepository.findByIdWithLock(listingId)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found"));

        // 2. Verify seller ownership
        if (!listing.getSeller().getId().equals(sellerId)) {
            throw new ListingOwnershipException(
                    "You are not authorized to accept requests for this listing"
            );
        }

        // 3. Find the transfer request
        TransferRequest transferRequest = transferRequestRepository.findById(requestId)
                .orElseThrow(() -> new TransferRequestException("Transfer request not found"));

        // 4. Verify request belongs to this listing
        if (!transferRequest.getListing().getId().equals(listingId)) {
            throw new TransferRequestException(
                    "Transfer request does not belong to this listing"
            );
        }

        // 5. Request must still be pending
        if (!"PENDING".equals(transferRequest.getStatus())) {
            throw new TransferRequestStateException(
                    "Only pending requests can be accepted"
            );
        }

        // 6. Listing must still be active
        if (!"ACTIVE".equals(listing.getListingStatus())) {
            throw new ListingStateException(
                    "Listing is no longer available"
            );
        }

        // 7. Accept the request
        transferRequest.setStatus("ACCEPTED");
        transferRequest.setRespondedAt(OffsetDateTime.now());

        // Reject all other pending requests for this listing
        List<TransferRequest> pendingRequests =
                transferRequestRepository.findByListingIdAndStatus(
                        listingId,
                        "PENDING"
                );

        OffsetDateTime now = OffsetDateTime.now();

        for (TransferRequest request : pendingRequests) {
            if (!request.getId().equals(requestId)) {
                request.setStatus("REJECTED");
                request.setRespondedAt(now);
            }
        }

        transferRequestRepository.saveAll(pendingRequests);

        listing.setListingStatus("TRANSFER_IN_PROGRESS");
        membershipListingRepository.save(listing);

        TransferRequest savedRequest = transferRequestRepository.save(transferRequest);

        return new TransferRequestResponse(
                savedRequest.getId(),
                savedRequest.getListing().getId(),
                savedRequest.getBuyer().getId(),
                savedRequest.getStatus(),
                savedRequest.getMessage(),
                savedRequest.getRequestedAt()
        );
    }

    @Transactional
    public TransferRequestResponse rejectTransferRequest(
            Long listingId,
            Long requestId,
            Long sellerId
    ) {
        // 1. Find the listing
        MembershipListing listing = membershipListingRepository.findByIdWithLock(listingId)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found"));

        // 2. Verify seller ownership
        if (!listing.getSeller().getId().equals(sellerId)) {
            throw new ListingOwnershipException(
                    "You are not authorized to reject requests for this listing"
            );
        }

        if (!"ACTIVE".equals(listing.getListingStatus())) {
            throw new ListingStateException(
                    "Requests can only be rejected for active listings"
            );
        }

        // 3. Find the transfer request
        TransferRequest transferRequest = transferRequestRepository.findById(requestId)
                .orElseThrow(() -> new TransferRequestException("Transfer request not found"));

        // 4. Verify request belongs to this listing
        if (!transferRequest.getListing().getId().equals(listingId)) {
            throw new TransferRequestException(
                    "Transfer request does not belong to this listing"
            );
        }

        // 5. Request must be pending
        if (!"PENDING".equals(transferRequest.getStatus())) {
            throw new TransferRequestStateException(
                    "Only pending requests can be rejected"
            );
        }

        // 6. Reject the request
        transferRequest.setStatus("REJECTED");
        transferRequest.setRespondedAt(OffsetDateTime.now());

        TransferRequest savedRequest = transferRequestRepository.save(transferRequest);

        return new TransferRequestResponse(
                savedRequest.getId(),
                savedRequest.getListing().getId(),
                savedRequest.getBuyer().getId(),
                savedRequest.getStatus(),
                savedRequest.getMessage(),
                savedRequest.getRequestedAt()
        );
    }

    @Transactional
    public TransferRequestResponse completeTransfer(
            Long listingId,
            Long requestId,
            Long buyerId
    ) {
        // 1. Find the listing
        MembershipListing listing = membershipListingRepository.findByIdWithLock(listingId)
                .orElseThrow(() -> new ListingNotFoundException("Listing not found"));

        // 2. Find the transfer request
        TransferRequest transferRequest = transferRequestRepository.findById(requestId)
                .orElseThrow(() -> new TransferRequestException("Transfer request not found"));

        // 3. Verify request belongs to this listing
        if (!transferRequest.getListing().getId().equals(listingId)) {
            throw new TransferRequestException(
                    "Transfer request does not belong to this listing"
            );
        }

        // 4. Verify buyer ownership
        if (!transferRequest.getBuyer().getId().equals(buyerId)) {
            throw new ListingOwnershipException(
                    "You are not authorized to complete this transfer"
            );
        }

        // 5. Request must be accepted
        if (!"ACCEPTED".equals(transferRequest.getStatus())) {
            throw new TransferRequestStateException(
                    "Only accepted requests can be completed"
            );
        }

        // 6. Listing must be in progress
        if (!"TRANSFER_IN_PROGRESS".equals(listing.getListingStatus())) {
            throw new ListingStateException(
                    "Listing is not in transfer progress"
            );
        }

        // 7. Complete the transfer
        transferRequest.setStatus("COMPLETED");
        transferRequest.setCompletedAt(OffsetDateTime.now());

        listing.setListingStatus("SOLD");

        transferRequestRepository.save(transferRequest);
        membershipListingRepository.save(listing);

        return new TransferRequestResponse(
                transferRequest.getId(),
                listing.getId(),
                transferRequest.getBuyer().getId(),
                transferRequest.getStatus(),
                transferRequest.getMessage(),
                transferRequest.getRequestedAt()
        );
    }
}