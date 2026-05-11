SET search_path TO core;

-- Add sender_phone to inquiries table
ALTER TABLE inquiries ADD COLUMN IF NOT EXISTS sender_phone VARCHAR(20);

-- Add sender_phone to professional_enquiries table
ALTER TABLE professional_enquiries ADD COLUMN IF NOT EXISTS sender_phone VARCHAR(20);
