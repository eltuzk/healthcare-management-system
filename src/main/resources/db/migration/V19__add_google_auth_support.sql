-- V18: Add Google OAuth support
-- Add google_id column to ACCOUNT table
ALTER TABLE ACCOUNT ADD google_id VARCHAR2(255) NULL;

-- Make password_hash nullable (Google users don't have a password)
ALTER TABLE ACCOUNT MODIFY password_hash NULL;

-- Add unique constraint on google_id
ALTER TABLE ACCOUNT ADD CONSTRAINT uq_account_google_id UNIQUE (google_id);
