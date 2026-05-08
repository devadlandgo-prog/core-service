SET search_path TO core;

CREATE INDEX IF NOT EXISTS idx_vendor_user ON vendor_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_land_vendor ON lands(vendor_id);
CREATE INDEX IF NOT EXISTS idx_land_status ON lands(status);
CREATE INDEX IF NOT EXISTS idx_land_city ON lands(city);
CREATE INDEX IF NOT EXISTS idx_reviews_professional_id ON reviews(professional_id);
CREATE INDEX IF NOT EXISTS idx_fav_user ON favorite_listings(user_id);
CREATE INDEX IF NOT EXISTS idx_inq_listing ON inquiries(listing_id);
CREATE INDEX IF NOT EXISTS idx_inq_prof ON inquiries(professional_id);
