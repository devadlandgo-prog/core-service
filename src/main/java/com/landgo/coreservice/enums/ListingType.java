package com.landgo.coreservice.enums;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Transaction types the filter accepts.
 *
 * <p>Create offers a "Selling Type" instead - Land Sale, Joint Venture, Builder Takeover - and
 * writes that label into {@code projectSpecification.sellingType}, with {@code listingType} left
 * unset unless the label was Land Sale. "Land Sale" is a sale, so it is aliased onto {@link #SALE}
 * here; Joint Venture and Builder Takeover are commercial arrangements rather than transaction
 * types and deliberately have no alias, because whether they become their own field, their own
 * enum values, or neither is an open product decision. Mapping them onto SALE would quietly
 * misfile them.
 *
 * <p>The bare {@code land} alias is not hypothetical: it is what the only listing in dev carrying a
 * selling type actually stores, so some client wrote the slug rather than the label and
 * {@code listingType=sale} matched nothing without it.
 */
public enum ListingType {
    SALE("land_sale", "land", "for_sale"),
    LEASE("for_lease"),
    RENT("for_rent"),
    AUCTION;

    private final Set<String> legacyTokens;

    ListingType(String... legacyTokens) {
        this.legacyTokens = new LinkedHashSet<>(Arrays.asList(legacyTokens));
    }

    /** Every normalised token that should match this type: its own name plus the legacy labels. */
    public Set<String> matchTokens() {
        Set<String> tokens = new LinkedHashSet<>();
        tokens.add(name().toLowerCase());
        tokens.addAll(legacyTokens);
        return tokens;
    }

    /**
     * The transaction type a submitted label denotes, for normalising a listing on write. Empty for
     * an unrecognised label, which is then stored as sent rather than guessed at, and for the
     * arrangements - see {@link ArrangementType}.
     */
    public static Optional<ListingType> canonicalFor(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String token = value.trim().toLowerCase().replace(' ', '_').replace('-', '_');
        return Arrays.stream(values())
                .filter(type -> type.matchTokens().contains(token))
                .findFirst();
    }
}
