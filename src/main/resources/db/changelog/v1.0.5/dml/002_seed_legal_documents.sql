SET search_path TO core;

INSERT INTO legal_documents (id, document_type, title, locale, version, updated_at, content_html)
VALUES
  ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'privacy', 'Privacy Policy', 'en-CA', '2026-03-07', '2026-03-07 00:00:00', '<h1>Privacy Policy</h1><p>We collect information you provide...</p><h2>1. Information We Collect</h2><p>We collect personal information such as name, email address, and contact information to provide our services.</p>'),
  ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'terms', 'Terms of Service', 'en-CA', '2026-03-07', '2026-03-07 00:00:00', '<h1>Terms of Service</h1><p>Welcome to LandGo. By using our services, you agree to these terms...</p><h2>1. Acceptance of Terms</h2><p>Please read these terms carefully before accessing or using our platform.</p>')
ON CONFLICT (document_type) DO UPDATE 
SET title = EXCLUDED.title,
    locale = EXCLUDED.locale,
    version = EXCLUDED.version,
    updated_at = EXCLUDED.updated_at,
    content_html = EXCLUDED.content_html;
