SET search_path TO core;

ALTER TABLE inquiries ALTER COLUMN sender_name DROP NOT NULL;
ALTER TABLE inquiries ALTER COLUMN sender_email DROP NOT NULL;
