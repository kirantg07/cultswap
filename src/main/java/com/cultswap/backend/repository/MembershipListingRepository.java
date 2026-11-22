package com.cultswap.backend.repository;

import com.cultswap.backend.entity.MembershipListing;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MembershipListingRepository
        extends JpaRepository<MembershipListing, Long> {

    Page<MembershipListing> findByListingStatus(
            String listingStatus,
            Pageable pageable
    );

    Page<MembershipListing> findBySellerId(
            Long sellerId,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM MembershipListing l WHERE l.id = :listingId")
    Optional<MembershipListing> findByIdWithLock(
            @Param("listingId") Long listingId
    );
}