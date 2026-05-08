SET search_path TO core;

INSERT INTO expertise_options (name, is_active)
VALUES
  ('Land Surveying', true),
  ('Architecture', true),
  ('Legal Advice', true),
  ('Civil Engineering', true),
  ('Environmental Assessment', true),
  ('Urban Planning', true),
  ('Real Estate Law', true),
  ('Property Appraisal', true)
ON CONFLICT (name) DO NOTHING;

INSERT INTO locations (id, name, code, type, parent_id, is_active, deleted, created_at, updated_at)
VALUES
  ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Canada', 'CA', 'COUNTRY', NULL, true, false, now(), now()),
  ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'Ontario', 'ON', 'STATE', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', true, false, now(), now()),
  ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'Toronto', 'TOR', 'CITY', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', true, false, now(), now())
ON CONFLICT (id) DO NOTHING;
