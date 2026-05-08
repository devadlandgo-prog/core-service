SET search_path TO core;

CREATE TABLE IF NOT EXISTS users (
  id UUID PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  full_name VARCHAR(255),
  profile_image_url VARCHAR(500),
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS vendor_profiles (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL UNIQUE REFERENCES users(id),
  company_name VARCHAR(100) NOT NULL,
  company_description TEXT,
  company_logo VARCHAR(500),
  business_license VARCHAR(255),
  business_address VARCHAR(255) NOT NULL,
  business_city VARCHAR(100) NOT NULL,
  business_state VARCHAR(100) NOT NULL,
  business_zip_code VARCHAR(20) NOT NULL,
  business_country VARCHAR(100) NOT NULL,
  website VARCHAR(255),
  verified BOOLEAN NOT NULL DEFAULT FALSE,
  verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  license_number VARCHAR(50),
  years_of_experience INTEGER,
  phone_number VARCHAR(20),
  bio TEXT,
  verification_notes VARCHAR(500),
  rating DECIMAL(3,2),
  total_reviews INTEGER DEFAULT 0,
  total_lands_listed INTEGER DEFAULT 0,
  total_lands_sold INTEGER DEFAULT 0,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS lands (
  id UUID PRIMARY KEY,
  vendor_id UUID NOT NULL,
  project_stage VARCHAR(50) NOT NULL,
  status VARCHAR(50) NOT NULL DEFAULT 'PENDING_APPROVAL',
  address VARCHAR(255) NOT NULL,
  city VARCHAR(100) NOT NULL,
  postal_code VARCHAR(20) NOT NULL,
  lot_size DECIMAL(15,2) NOT NULL,
  lot_unit VARCHAR(20) NOT NULL,
  frontage VARCHAR(50),
  depth VARCHAR(50),
  current_zoning_codes VARCHAR(255),
  pin_number VARCHAR(50),
  official_plan_designation VARCHAR(255),
  latitude DECIMAL(10,8),
  longitude DECIMAL(11,8),
  project_specification JSONB,
  asking_price DECIMAL(15,2) NOT NULL,
  currency VARCHAR(10) NOT NULL DEFAULT 'CAD',
  pricing_description TEXT,
  photos JSONB,
  documents JSONB,
  ownership_verification VARCHAR(500),
  view_count INTEGER DEFAULT 0,
  inquiry_count INTEGER DEFAULT 0,
  is_featured BOOLEAN NOT NULL DEFAULT FALSE,
  is_developer_deal BOOLEAN NOT NULL DEFAULT FALSE,
  is_hot_deal BOOLEAN NOT NULL DEFAULT FALSE,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS listing_drafts (
  id UUID PRIMARY KEY,
  owner_id UUID NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
  current_step INTEGER DEFAULT 0,
  step1_data JSONB,
  step2_data JSONB,
  step3_data JSONB,
  step4_data JSONB,
  step5_data JSONB,
  published_listing_id UUID REFERENCES lands(id),
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS reviews (
  id UUID PRIMARY KEY,
  professional_id UUID NOT NULL,
  author_id UUID NOT NULL,
  rating INTEGER NOT NULL,
  title VARCHAR(200) NOT NULL,
  content TEXT NOT NULL,
  verified_purchase BOOLEAN NOT NULL DEFAULT FALSE,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(author_id, professional_id)
);

CREATE TABLE IF NOT EXISTS saved_searches (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL,
  name VARCHAR(100) NOT NULL,
  keyword VARCHAR(200),
  city VARCHAR(100),
  project_stage VARCHAR(30),
  min_price DECIMAL(15,2),
  max_price DECIMAL(15,2),
  min_lot_size DECIMAL(15,2),
  max_lot_size DECIMAL(15,2),
  notifications_enabled BOOLEAN DEFAULT TRUE,
  match_count INTEGER DEFAULT 0,
  last_notified_at TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS favorite_listings (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL,
  land_id UUID NOT NULL REFERENCES lands(id) ON DELETE CASCADE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(user_id, land_id)
);

CREATE TABLE IF NOT EXISTS inquiries (
  id UUID PRIMARY KEY,
  listing_id UUID REFERENCES lands(id) ON DELETE CASCADE,
  professional_id UUID REFERENCES vendor_profiles(user_id) ON DELETE CASCADE,
  sender_name VARCHAR(255) NOT NULL,
  sender_email VARCHAR(255) NOT NULL,
  message TEXT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS expertise_options (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name VARCHAR(100) NOT NULL UNIQUE,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS vendor_specializations (
  vendor_id UUID NOT NULL REFERENCES vendor_profiles(id) ON DELETE CASCADE,
  specialization VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS vendor_service_areas (
  vendor_id UUID NOT NULL REFERENCES vendor_profiles(id) ON DELETE CASCADE,
  service_area VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS vendor_certifications (
  vendor_id UUID NOT NULL REFERENCES vendor_profiles(id) ON DELETE CASCADE,
  certification VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS review_tags (
  review_id UUID NOT NULL REFERENCES reviews(id) ON DELETE CASCADE,
  tag VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS review_photos (
  review_id UUID NOT NULL REFERENCES reviews(id) ON DELETE CASCADE,
  photo_url VARCHAR(2000)
);

CREATE TABLE IF NOT EXISTS professional_enquiries (
  id UUID PRIMARY KEY,
  vendor_id UUID NOT NULL,
  sender_id UUID,
  sender_name VARCHAR(255) NOT NULL,
  sender_email VARCHAR(255) NOT NULL,
  message TEXT NOT NULL,
  status VARCHAR(50) NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS locations (
  id UUID PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  code VARCHAR(10) NOT NULL,
  type VARCHAR(20) NOT NULL,
  parent_id UUID,
  is_active BOOLEAN DEFAULT TRUE,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
