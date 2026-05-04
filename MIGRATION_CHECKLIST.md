# Production Migration Checklist - Backend Enhancements

This checklist covers the steps required to migrate the backend to support real logic for hot deals, featured listings, and view counts.

## 1. Database Migrations
- [ ] Run Liquibase migrations to add `is_featured` and `is_hot_deal` columns to the `lands` table.
  - Migration ID: `6-add-featured-and-hot-deal-to-lands`
  - SQL: `ALTER TABLE lands ADD COLUMN IF NOT EXISTS is_featured BOOLEAN NOT NULL DEFAULT FALSE, ADD COLUMN IF NOT EXISTS is_hot_deal BOOLEAN NOT NULL DEFAULT FALSE;`

## 2. Environment Configuration
- No new environment variables are required for this update.
- Ensure the database user has permissions to execute `ALTER TABLE` and `CREATE TABLE` (for element collections added in previous migrations).

## 3. Data Population (Optional but Recommended)
- [ ] Tag specific high-value listings as "featured" or "hot deal" in the database to verify the new endpoints:
  ```sql
  UPDATE core.lands SET is_featured = true WHERE id = 'some-listing-uuid';
  UPDATE core.lands SET is_hot_deal = true WHERE id = 'another-listing-uuid';
  ```

## 4. Verification Steps
- [ ] Verify `GET /api/listings/hot-developer` returns only listings with `is_hot_deal = true`.
- [ ] Verify `GET /api/listings/featured` returns only listings with `is_featured = true`.
- [ ] Verify `POST /api/listings/{id}/view` increments the `view_count` in the database and returns the correct new value.
- [ ] Verify `LandResponse` DTO now includes `isFeatured` and `isHotDeal` boolean fields.

## 5. Rollback Plan
- In case of failure, the code can be reverted. The database columns can remain as they have defaults and won't break existing queries.
