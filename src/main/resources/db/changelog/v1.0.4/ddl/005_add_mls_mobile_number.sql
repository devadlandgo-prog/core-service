-- Add mls_mobile_number column to lands table
ALTER TABLE lands ADD COLUMN IF NOT EXISTS mls_mobile_number VARCHAR(20);
