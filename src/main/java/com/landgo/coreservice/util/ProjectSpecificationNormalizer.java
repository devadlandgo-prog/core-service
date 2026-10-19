package com.landgo.coreservice.util;

import com.landgo.coreservice.enums.ArrangementType;
import com.landgo.coreservice.enums.BuildingType;
import com.landgo.coreservice.enums.ListingType;
import com.landgo.coreservice.enums.ProjectType;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

/**
 * Rewrites the type fields of a submitted {@code projectSpecification} into the values the filter
 * endpoints publish, so a listing is stored under the same vocabulary it will be searched by.
 *
 * <p>Until now the create screens' display labels went to the database untouched - "Detached",
 * "Land Sale", "Commercial Plaza" - while the filters compared against canonical enum names, so a
 * listing was invisible to the filter that should have found it. The read side carries aliases for
 * the rows already stored that way; this is the write side, so the problem stops growing.
 *
 * <p>A label that maps to nothing is left exactly as sent. Normalising is for labels whose meaning
 * is unambiguous, not an excuse to discard a value nobody here recognises.
 */
public final class ProjectSpecificationNormalizer {

    private static final String BUILDING_TYPE = "buildingType";
    private static final String LISTING_TYPE = "listingType";
    private static final String SELLING_TYPE = "sellingType";
    private static final String ARRANGEMENT_TYPE = "arrangementType";
    private static final String PROJECT_TYPE = "projectType";
    private static final String PROPOSED_USE = "proposedUse";

    private ProjectSpecificationNormalizer() {
    }

    /** Normalises in place and returns the same map, for use inline in a mapper. */
    public static Map<String, Object> normalize(Map<String, Object> spec) {
        if (spec == null || spec.isEmpty()) {
            return spec;
        }

        BuildingType.canonicalFor(asText(spec.get(BUILDING_TYPE)))
                .ifPresent(type -> spec.put(BUILDING_TYPE, type.name()));

        ListingType.canonicalFor(asText(spec.get(LISTING_TYPE)))
                .ifPresent(type -> spec.put(LISTING_TYPE, type.name()));

        canonicalProjectType(asText(spec.get(PROJECT_TYPE)))
                .ifPresent(type -> spec.put(PROJECT_TYPE, type.name()));

        // proposedUse carries a land-use category on some stages and a CSV of application types
        // ("Rezoning, Site Plan") on others. Only the first shape maps to anything, and the second
        // is left alone rather than mangled.
        canonicalProjectType(asText(spec.get(PROPOSED_USE)))
                .ifPresent(type -> spec.put(PROPOSED_USE, type.name()));

        normalizeSellingType(spec);
        return spec;
    }

    /**
     * Splits the create screen's single "Selling Type" into the two things it conflates.
     *
     * <p>Land Sale is a transaction and becomes {@code listingType}; Joint Venture and Builder
     * Takeover are commercial arrangements and become {@code arrangementType}. Neither overwrites a
     * value the client set explicitly, and {@code sellingType} is kept as submitted so nothing is
     * lost in translation.
     */
    private static void normalizeSellingType(Map<String, Object> spec) {
        String sellingType = asText(spec.get(SELLING_TYPE));
        if (sellingType == null) {
            return;
        }

        Optional<ArrangementType> arrangement = ArrangementType.fromLabel(sellingType);
        if (arrangement.isPresent()) {
            if (asText(spec.get(ARRANGEMENT_TYPE)) == null) {
                spec.put(ARRANGEMENT_TYPE, arrangement.get().name());
            }
            return;
        }

        if (asText(spec.get(LISTING_TYPE)) == null) {
            ListingType.canonicalFor(sellingType)
                    .ifPresent(type -> spec.put(LISTING_TYPE, type.name()));
        }
    }

    private static Optional<ProjectType> canonicalProjectType(String value) {
        if (value == null) {
            return Optional.empty();
        }
        String token = value.trim().toUpperCase().replace(' ', '_').replace('-', '_');
        return Arrays.stream(ProjectType.values())
                .filter(type -> type.name().equals(token))
                .findFirst();
    }

    private static String asText(Object value) {
        if (!(value instanceof String text)) {
            return null;
        }
        return text.isBlank() ? null : text;
    }
}
