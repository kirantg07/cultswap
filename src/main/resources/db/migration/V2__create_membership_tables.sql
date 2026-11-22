-- Membership listings
CREATE TABLE membership_listings (
                                     id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                     seller_id BIGINT NOT NULL,

                                     membership_type VARCHAR(100) NOT NULL,

                                     purchase_date DATE NOT NULL,

                                     expiry_date DATE NOT NULL,

                                     asking_price NUMERIC(10, 2) NOT NULL,

                                     city VARCHAR(100) NOT NULL,

                                     listing_status VARCHAR(30) NOT NULL DEFAULT 'PENDING_VERIFICATION',

                                     document_verification_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

                                     transfer_eligibility_status VARCHAR(30) NOT NULL DEFAULT 'UNKNOWN',

                                     version BIGINT NOT NULL DEFAULT 0,

                                     created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                     updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                     CONSTRAINT fk_listing_seller
                                         FOREIGN KEY (seller_id)
                                             REFERENCES users(id),

                                     CONSTRAINT chk_listing_price
                                         CHECK (asking_price > 0),

                                     CONSTRAINT chk_listing_dates
                                         CHECK (expiry_date > purchase_date)
);


-- Membership documents
CREATE TABLE membership_documents (
                                      id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                      listing_id BIGINT NOT NULL,

                                      storage_key VARCHAR(500) NOT NULL,

                                      document_type VARCHAR(50) NOT NULL,

                                      document_status VARCHAR(30) NOT NULL DEFAULT 'UPLOADED',

                                      uploaded_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                      CONSTRAINT fk_document_listing
                                          FOREIGN KEY (listing_id)
                                              REFERENCES membership_listings(id)
);


-- Verification attempts
CREATE TABLE verification_attempts (
                                       id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

                                       listing_id BIGINT NOT NULL,

                                       verification_type VARCHAR(50) NOT NULL,

                                       status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

                                       confidence_score NUMERIC(5, 2),

                                       failure_reason TEXT,

                                       verified_at TIMESTAMPTZ,

                                       created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                       CONSTRAINT fk_verification_listing
                                           FOREIGN KEY (listing_id)
                                               REFERENCES membership_listings(id),

                                       CONSTRAINT chk_confidence_score
                                           CHECK (
                                               confidence_score IS NULL
                                                   OR confidence_score BETWEEN 0 AND 100
                                               )
);


-- Indexes
CREATE INDEX idx_listings_seller
    ON membership_listings(seller_id);

CREATE INDEX idx_listings_city_status
    ON membership_listings(city, listing_status);

CREATE INDEX idx_listings_price
    ON membership_listings(asking_price);

CREATE INDEX idx_verification_listing
    ON verification_attempts(listing_id);