SET search_path TO core;

CREATE TABLE IF NOT EXISTS faqs (
    id UUID PRIMARY KEY,
    question VARCHAR(200) NOT NULL,
    answer TEXT NOT NULL,
    answer_html TEXT,
    category VARCHAR(100),
    sort_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- The public listing reads live rows in display order on every page load, so the
-- index covers exactly that: the deleted filter plus both sort keys.
CREATE INDEX IF NOT EXISTS idx_faqs_live_order
    ON faqs (sort_order, question)
    WHERE deleted = FALSE;

CREATE INDEX IF NOT EXISTS idx_faqs_category
    ON faqs (category)
    WHERE deleted = FALSE;
