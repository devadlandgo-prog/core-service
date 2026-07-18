SET search_path TO core;

CREATE TABLE IF NOT EXISTS legal_documents (
    id UUID PRIMARY KEY,
    document_type VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    locale VARCHAR(20) NOT NULL,
    version VARCHAR(50),
    updated_at TIMESTAMP NOT NULL,
    content_html TEXT NOT NULL
);
