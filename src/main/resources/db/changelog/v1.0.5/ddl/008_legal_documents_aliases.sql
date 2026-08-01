SET search_path TO core;

-- Alternate slugs a legal document can also be fetched by (GET /legal/{alias}).
-- Stored as a JSONB array of lowercase kebab-case strings, e.g. ["cookies"].
ALTER TABLE legal_documents ADD COLUMN IF NOT EXISTS aliases JSONB NOT NULL DEFAULT '[]'::jsonb;

-- Protected documents cannot be removed via DELETE /legal/{documentType}
-- because public web pages depend on them (/privacy, /terms).
ALTER TABLE legal_documents ADD COLUMN IF NOT EXISTS protected BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE legal_documents SET protected = TRUE WHERE document_type IN ('privacy', 'terms');

UPDATE legal_documents SET aliases = '["privacy-policy"]'::jsonb WHERE document_type = 'privacy';
UPDATE legal_documents SET aliases = '["terms-of-service"]'::jsonb WHERE document_type = 'terms';
