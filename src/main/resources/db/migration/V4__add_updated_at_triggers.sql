-- Common function to update updated_at automatically
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- Users table
CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();


-- Membership listings table
CREATE TRIGGER trg_membership_listings_updated_at
    BEFORE UPDATE ON membership_listings
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();


-- Transfer requests table
CREATE TRIGGER trg_transfer_requests_updated_at
    BEFORE UPDATE ON transfer_requests
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();