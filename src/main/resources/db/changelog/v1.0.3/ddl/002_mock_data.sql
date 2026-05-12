SET search_path TO core;

-- Featured Listing: High-rise development site in Toronto
INSERT INTO lands (id, vendor_id, project_stage, status, address, city, postal_code, lot_size, lot_unit, asking_price, is_featured, is_developer_deal, is_hot_deal, pricing_description)
VALUES ('6f2a7b3c-5d8e-4f1a-9c2b-3a1b4c5d6e7f', '550e8400-e29b-41d4-a716-446655440001', 'READY_TO_SHOVEL', 'ACTIVE', '450 Front Street West', 'Toronto', 'M5V 1B6', 0.85, 'ACRES', 24500000.00, true, false, false, 'Fully approved for a 42-storey residential tower with mixed-use retail at grade.')
ON CONFLICT (id) DO NOTHING;

-- Hot Deal: Multi-residential assembly in Mississauga
INSERT INTO lands (id, vendor_id, project_stage, status, address, city, postal_code, lot_size, lot_unit, asking_price, is_featured, is_developer_deal, is_hot_deal, pricing_description)
VALUES ('7a1b2c3d-4e5f-6a7b-8c9d-0e1f2a3b4c5d', '550e8400-e29b-41d4-a716-446655440002', 'SITE_PLAN_APPROVAL', 'ACTIVE', '1288 Hurontario St', 'Mississauga', 'L5G 3H3', 1.2, 'ACRES', 8900000.00, false, false, true, 'Prime location near the future LRT line. Motivated seller, priced below recent appraisals.')
ON CONFLICT (id) DO NOTHING;

-- Developer Deal: Industrial conversion in Hamilton
INSERT INTO lands (id, vendor_id, project_stage, status, address, city, postal_code, lot_size, lot_unit, asking_price, is_featured, is_developer_deal, is_hot_deal, pricing_description)
VALUES ('8b2c3d4e-5f6a-7b8c-9d0e-1f2a3b4c5d6e', '550e8400-e29b-41d4-a716-446655440003', 'DRAFT_PLAN_APPROVAL', 'ACTIVE', '75 Burlington St E', 'Hamilton', 'L8L 4G5', 4.5, 'ACRES', 12400000.00, false, true, false, 'Large industrial parcel with zoning favorability for high-density residential conversion.')
ON CONFLICT (id) DO NOTHING;

-- Regular Listing: Suburban subdivision site
INSERT INTO lands (id, vendor_id, project_stage, status, address, city, postal_code, lot_size, lot_unit, asking_price, is_featured, is_developer_deal, is_hot_deal, pricing_description)
VALUES ('9c3d4e5f-6a7b-8c9d-0e1f-2a3b4c5d6e7f', '550e8400-e29b-41d4-a716-446655440004', 'UNDER_CITY_SUBMISSION', 'ACTIVE', '3400 Major MacKenzie Dr W', 'Vaughan', 'L6A 1T1', 12.0, 'ACRES', 35000000.00, false, false, false, 'Draft plan submitted for 45 detached residential lots. Excellent connectivity to Hwy 400.')
ON CONFLICT (id) DO NOTHING;
