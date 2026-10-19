package com.landgo.coreservice.enums;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * How a project is handed over commercially, as opposed to {@link ListingType}, which is the
 * transaction.
 *
 * <p>The create screen's "Selling Type" offers Land Sale, Joint Venture and Builder Takeover as one
 * list, and only the first is a transaction type. The other two were landing in the same
 * {@code sellingType} string with nowhere canonical to go, so they were unfilterable: a filter for
 * {@code listingType=joint_venture} was rejected outright, and folding them into SALE would have
 * misfiled a joint venture as a sale. They get their own field instead.
 *
 * <p>A listing can hold both - a joint venture is still offered for sale - so this never replaces
 * {@link ListingType}.
 */
public enum ArrangementType {
    JOINT_VENTURE("joint_venture", "jv", "joint_ventures"),
    BUILDER_TAKEOVER("builder_takeover", "builder_take_over", "takeover");

    private final Set<String> matchTokens;

    ArrangementType(String... tokens) {
        Set<String> all = new LinkedHashSet<>();
        all.add(name().toLowerCase());
        all.addAll(Arrays.asList(tokens));
        this.matchTokens = all;
    }

    /** Every normalised token that should match this arrangement, canonical name included. */
    public Set<String> matchTokens() {
        return matchTokens;
    }

    /**
     * The arrangement a stored or submitted label denotes, if any.
     *
     * <p>Empty for "Land Sale" and anything else that is a transaction rather than an arrangement -
     * the caller keeps treating those as a {@link ListingType}.
     */
    public static Optional<ArrangementType> fromLabel(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String token = value.trim().toLowerCase().replace(' ', '_').replace('-', '_');
        return Arrays.stream(values())
                .filter(type -> type.matchTokens.contains(token))
                .findFirst();
    }
}
