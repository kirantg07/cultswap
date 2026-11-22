
CREATE TABLE transfer_requests (
                                   id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                   listing_id BIGINT NOT NULL,

                                   buyer_id BIGINT NOT NULL,

                                   status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

                                   message TEXT,

                                   requested_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                   responded_at TIMESTAMPTZ,

                                   completed_at TIMESTAMPTZ,

                                   updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                   CONSTRAINT fk_transfer_listing
                                       FOREIGN KEY (listing_id)
                                           REFERENCES membership_listings(id),

                                   CONSTRAINT fk_transfer_buyer
                                       FOREIGN KEY (buyer_id)
                                           REFERENCES users(id),

                                   CONSTRAINT chk_transfer_status
                                       CHECK (
                                           status IN (
                                                      'PENDING',
                                                      'ACCEPTED',
                                                      'REJECTED',
                                                      'CANCELLED',
                                                      'COMPLETED'
                                               )
                                           )
);


-- Find requests for a particular listing
CREATE INDEX idx_transfer_listing
    ON transfer_requests(listing_id);


-- Find requests submitted by a buyer
CREATE INDEX idx_transfer_buyer
    ON transfer_requests(buyer_id);


-- Find pending requests for seller review
CREATE INDEX idx_transfer_pending
    ON transfer_requests(listing_id, requested_at)
    WHERE status = 'PENDING';


-- Prevent the same buyer from submitting multiple
-- simultaneously pending requests for the same listing
CREATE UNIQUE INDEX uq_pending_request_per_buyer_listing
    ON transfer_requests(listing_id, buyer_id)
    WHERE status = 'PENDING';


-- Ensure only one request can be accepted for a listing
CREATE UNIQUE INDEX uq_accepted_request_per_listing
    ON transfer_requests(listing_id)
    WHERE status = 'ACCEPTED';
