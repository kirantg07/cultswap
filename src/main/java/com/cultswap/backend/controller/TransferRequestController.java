package com.cultswap.backend.controller;

import com.cultswap.backend.dto.CreateTransferRequest;
import com.cultswap.backend.dto.TransferRequestResponse;
import com.cultswap.backend.service.TransferRequestService;

import jakarta.validation.Valid;

import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/listings")
@Validated
public class TransferRequestController {

    private final TransferRequestService transferRequestService;

    public TransferRequestController(
            TransferRequestService transferRequestService
    ) {
        this.transferRequestService = transferRequestService;
    }

    @PostMapping("/{listingId}/transfer-requests")
    @ResponseStatus(HttpStatus.CREATED)
    public TransferRequestResponse createTransferRequest(
            @PathVariable @Min(1) Long listingId,
            @Valid @RequestBody CreateTransferRequest request,
            @AuthenticationPrincipal Long buyerId
    ) {
        return transferRequestService.createTransferRequest(
                listingId,
                buyerId,
                request.getMessage()
        );
    }

    @GetMapping("/{listingId}/transfer-requests")
    public List<TransferRequestResponse> getListingTransferRequests(
            @PathVariable Long listingId,
            @AuthenticationPrincipal Long sellerId
    ) {
        return transferRequestService.getListingTransferRequests(
                listingId,
                sellerId
        );
    }

    @PutMapping("/{listingId}/transfer-requests/{requestId}/accept")
    public TransferRequestResponse acceptTransferRequest(
            @PathVariable Long listingId,
            @PathVariable Long requestId,
            @AuthenticationPrincipal Long sellerId
    ) {
        return transferRequestService.acceptTransferRequest(
                listingId,
                requestId,
                sellerId
        );
    }

    @PutMapping("/{listingId}/transfer-requests/{requestId}/reject")
    public TransferRequestResponse rejectTransferRequest(
            @PathVariable Long listingId,
            @PathVariable Long requestId,
            @AuthenticationPrincipal Long sellerId
    ) {
        return transferRequestService.rejectTransferRequest(
                listingId,
                requestId,
                sellerId
        );
    }

    @PutMapping("/{listingId}/transfer-requests/{requestId}/complete")
    public TransferRequestResponse completeTransfer(
            @PathVariable Long listingId,
            @PathVariable Long requestId,
            @AuthenticationPrincipal Long buyerId
    ) {
        return transferRequestService.completeTransfer(
                listingId,
                requestId,
                buyerId
        );
    }
}