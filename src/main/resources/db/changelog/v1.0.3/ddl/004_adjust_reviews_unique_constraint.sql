-- Drop the original unique constraint that prevents active reviews from being re-submitted after soft delete
ALTER TABLE core.reviews DROP CONSTRAINT IF EXISTS reviews_author_id_professional_id_key;

-- Create a partial unique index allowing only one ACTIVE review per user per professional
CREATE UNIQUE INDEX IF NOT EXISTS idx_reviews_author_professional_active 
ON core.reviews (author_id, professional_id) 
WHERE (deleted = false);
