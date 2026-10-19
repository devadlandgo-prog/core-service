SET search_path TO core;

-- minLotSize/maxLotSize compared the raw lot_size column and ignored lot_unit entirely, so the
-- bounds only meant anything if every listing happened to use the same unit. They do not:
-- lot_unit is free-form and the create screen offers SQFT, ACRE and HECTARE. A one-acre lot
-- entered as 43560 SQFT fell outside "1 to 2 acres" while a 43560-acre lot fell inside, and a
-- 5-hectare lot sorted as though it were 5 acres. The filter inputs are acres, so acres is the
-- unit everything has to be compared in.
--
-- Generated rather than maintained in the service: the value is a pure function of two columns
-- already on the row, and computing it here means no write path can forget to update it and no
-- backfill can drift. STORED so it can be indexed.
--
-- An unrecognised or null unit falls through to the raw number, which is the pre-existing
-- behaviour and treats the value as already being acres. That is a guess, but it is the guess
-- the filter has always made, and inventing a different one would silently move listings in and
-- out of range.
ALTER TABLE lands
    ADD COLUMN IF NOT EXISTS lot_size_acres NUMERIC(20, 6)
    GENERATED ALWAYS AS (
        CASE
            WHEN lot_unit IS NULL THEN lot_size
            WHEN lower(btrim(lot_unit)) IN ('sqft', 'sq_ft', 'sqfeet', 'square_feet', 'squarefeet', 'ft2')
                THEN lot_size / 43560.0
            WHEN lower(btrim(lot_unit)) IN ('sqm', 'sq_m', 'square_meters', 'squaremeters', 'm2')
                THEN lot_size / 4046.8564224
            WHEN lower(btrim(lot_unit)) IN ('hectare', 'hectares', 'ha')
                THEN lot_size * 2.4710538147
            ELSE lot_size
        END
    ) STORED;

CREATE INDEX IF NOT EXISTS idx_lands_lot_size_acres ON lands (lot_size_acres);
