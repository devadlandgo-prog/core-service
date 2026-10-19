package com.landgo.coreservice.enums;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Canonical building types the filter accepts, each carrying the spellings a stored listing may
 * actually hold.
 *
 * <p>The create screens offer display labels - "Detached", "Townhouse Block", "Commercial Plaza" -
 * and the publish mapper writes them into {@code projectSpecification.buildingType} unchanged. The
 * filter compares against the canonical name, so every listing created through the app was
 * invisible to every building-type filter, including the correct one: {@code
 * buildingType=single_family} matched nothing because the rows say "Detached".
 *
 * <p>The aliases make the read side match that history. They are not a decision about what create
 * should write going forward - that taxonomy question, and the migration of stored labels, is still
 * open.
 */
public enum BuildingType {
    SINGLE_FAMILY("detached", "detached_home", "single_family_home"),
    /**
     * "Condo / Apartment" is listed here and on {@link #CONDO} on purpose. The create label does not
     * say which of the two canonical types it meant, so until that is decided a filter for either
     * one returns the listing rather than neither returning it.
     */
    MULTI_FAMILY("condo_/_apartment", "condo_apartment", "apartment"),
    CONDO("condo_/_apartment", "condo_apartment", "apartment"),
    TOWNHOUSE("townhouse_block", "town_house"),
    COMMERCIAL("commercial_plaza"),
    INDUSTRIAL("industrial_building"),
    RETAIL,
    OFFICE,
    WAREHOUSE,
    MIXED_USE;

    private final Set<String> legacyTokens;

    BuildingType(String... legacyTokens) {
        this.legacyTokens = new LinkedHashSet<>(Arrays.asList(legacyTokens));
    }

    /**
     * Every normalised token that should match this type: its own name plus the legacy labels.
     *
     * <p>Normalised the same way the query normalises a stored value - lower case, spaces and
     * hyphens as underscores - so "Condo / Apartment" arrives as {@code condo_/_apartment}, slash
     * included, because the stored text has one and only spaces around it collapse.
     */
    public Set<String> matchTokens() {
        Set<String> tokens = new LinkedHashSet<>();
        tokens.add(name().toLowerCase());
        tokens.addAll(legacyTokens);
        return tokens;
    }

    /**
     * The one canonical type a submitted label denotes, for normalising a listing on write.
     *
     * <p>Empty when the label matches none - so an unrecognised value is stored as sent rather than
     * guessed at - and equally when it matches more than one, which today means "Condo / Apartment"
     * and its variants. Picking CONDO or MULTI_FAMILY for that label is the open taxonomy decision;
     * until the create screen offers the two separately, the ambiguous label is left intact and the
     * read-side aliases keep it findable under both.
     */
    public static Optional<BuildingType> canonicalFor(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String token = value.trim().toLowerCase().replace(' ', '_').replace('-', '_');
        List<BuildingType> matches = Arrays.stream(values())
                .filter(type -> type.matchTokens().contains(token))
                .toList();
        return matches.size() == 1 ? Optional.of(matches.get(0)) : Optional.empty();
    }
}
