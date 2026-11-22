package com.cultswap.backend.repository;

import com.cultswap.backend.entity.TransferRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransferRequestRepository
        extends JpaRepository<TransferRequest, Long> {

    boolean existsByListingIdAndBuyerIdAndStatus(
            Long listingId,
            Long buyerId,
            String status
    );

    List<TransferRequest> findByListingId(Long listingId);

    List<TransferRequest> findByListingIdAndStatus(
            Long listingId,
            String status
    );

}