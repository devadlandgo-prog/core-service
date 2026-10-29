package com.landgo.coreservice.util;

import com.landgo.coreservice.enums.ArrangementType;
import com.landgo.coreservice.enums.ListingType;
import com.landgo.coreservice.exception.ApiException;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Requires every listing to say how it is offered.
 *
 * <p>{@code listingType} was optional and unvalidated, so most listings carried no transaction type
 * at all: only the Ready-to-Shovel stage has a picker, and only its "Land Sale" option produced one.
 * A listing with none is unfilterable by the thing buyers search on first.
 *
 * <p>Deliberately no default. Treating a missing value as SALE would be invisible and wrong for the
 * lease and auction listings the enum has always allowed, so a missing value is an error the seller
 * resolves, not a gap the server papers over.
 *
 * <p>Runs after {@link ProjectSpecificationNormalizer}, so the display labels the create screens
 * send are already canonical by the time they get here and "Land Sale" satisfies the requirement.
 *
 * <p>Enforced on create and on every update, including updates by an admin: a transaction type is
 * data quality rather than an entitlement, and a listing an admin posts is as unfilterable without
 * one as any other. Drafts are exempt - a half-finished listing is the point of a draft - and reads
 * are untouched, so a listing stored before the requirement keeps returning exactly what it holds
 * until someone edits it.
 */
public final class TransactionTypeValidator {

    private static final String FIELD = "projectSpecification.listingType";
    private static final List<String> ALLOWED =
            Arrays.stream(ListingType.values()).map(Enum::name).toList();

    private TransactionTypeValidator() {
    }

    /**
     * @param spec the normalised specification about to be persisted
     * @throws ApiException {@code LISTING_TYPE_REQUIRED} when absent, {@code LISTING_TYPE_INVALID}
     *                      when present but not a transaction type
     */
    public static void validate(Map<String, Object> spec) {
        Object raw = spec == null ? null : spec.get("listingType");
        String value = raw instanceof String text && !text.isBlank() ? text : null;

        if (value == null) {
            throw new ApiException(
                    "Select how this listing is offered: SALE, LEASE, RENT or AUCTION.",
                    HttpStatus.BAD_REQUEST,
                    "LISTING_TYPE_REQUIRED",
                    details(null));
        }

        if (ListingType.canonicalFor(value).isEmpty()) {
            // An arrangement reaching this field is the likely mistake, and the generic "invalid"
            // message would send someone hunting for a typo, so it is named for what it is.
            String message = ArrangementType.fromLabel(value).isPresent()
                    ? "Joint Venture and Builder Takeover describe the arrangement, not the "
                            + "transaction. Send them as arrangementType and still choose a "
                            + "listingType of SALE, LEASE, RENT or AUCTION."
                    : "Invalid listingType value: " + value;
            throw new ApiException(message, HttpStatus.BAD_REQUEST, "LISTING_TYPE_INVALID", details(value));
        }
    }

    private static Map<String, Object> details(String submitted) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("field", FIELD);
        if (submitted != null) {
            details.put("submitted", submitted);
        }
        details.put("allowed", ALLOWED);
        return details;
    }
}
