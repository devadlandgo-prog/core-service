package com.landgo.coreservice.util;

import com.landgo.coreservice.exception.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionTypeValidatorTest {

    /** Mirrors the real path: the normaliser runs first, then validation. */
    private static Map<String, Object> normalised(String... keyValues) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put(keyValues[i], keyValues[i + 1]);
        }
        return ProjectSpecificationNormalizer.normalize(map);
    }

    @Test
    @DisplayName("every transaction type is accepted")
    void acceptsEveryTransactionType() {
        for (String type : new String[] {"SALE", "LEASE", "RENT", "AUCTION"}) {
            assertDoesNotThrow(() -> TransactionTypeValidator.validate(normalised("listingType", type)));
        }
    }

    @Test
    @DisplayName("lower case is accepted")
    void acceptsLowerCase() {
        assertDoesNotThrow(() -> TransactionTypeValidator.validate(normalised("listingType", "lease")));
    }

    @Test
    @DisplayName("the create screen's Land Sale label satisfies the requirement")
    void acceptsLandSaleFromSellingType() {
        // No explicit listingType: the normaliser derives SALE from the selling type, and the
        // seller never had to answer a question the screen does not ask.
        assertDoesNotThrow(() -> TransactionTypeValidator.validate(normalised("sellingType", "Land Sale")));
    }

    @Test
    @DisplayName("a missing transaction type is rejected, never defaulted")
    void rejectsMissing() {
        ApiException ex = assertThrows(ApiException.class,
                () -> TransactionTypeValidator.validate(normalised("buildingType", "Detached")));
        assertEquals("LISTING_TYPE_REQUIRED", ex.getErrorCode());
        assertEquals(400, ex.getStatus().value());
    }

    @Test
    @DisplayName("an empty spec is rejected the same way")
    void rejectsEmptyAndNullSpec() {
        assertEquals("LISTING_TYPE_REQUIRED",
                assertThrows(ApiException.class, () -> TransactionTypeValidator.validate(new HashMap<>())).getErrorCode());
        assertEquals("LISTING_TYPE_REQUIRED",
                assertThrows(ApiException.class, () -> TransactionTypeValidator.validate(null)).getErrorCode());
    }

    @Test
    @DisplayName("an unrecognised value is invalid, not missing")
    void rejectsUnknownValue() {
        ApiException ex = assertThrows(ApiException.class,
                () -> TransactionTypeValidator.validate(normalised("listingType", "freehold")));
        assertEquals("LISTING_TYPE_INVALID", ex.getErrorCode());
        assertTrue(ex.getMessage().contains("freehold"));
    }

    @Test
    @DisplayName("an arrangement cannot stand in for a transaction type")
    void arrangementIsNotATransaction() {
        // Joint Venture populates arrangementType and leaves listingType unset, so the listing is
        // still missing the thing buyers filter on.
        Map<String, Object> spec = normalised("sellingType", "Joint Venture");
        assertEquals("JOINT_VENTURE", spec.get("arrangementType"));

        ApiException ex = assertThrows(ApiException.class, () -> TransactionTypeValidator.validate(spec));
        assertEquals("LISTING_TYPE_REQUIRED", ex.getErrorCode());
    }

    @Test
    @DisplayName("an arrangement sent as the transaction type says so plainly")
    void arrangementInListingTypeIsNamed() {
        Map<String, Object> spec = new HashMap<>();
        spec.put("listingType", "Joint Venture");

        ApiException ex = assertThrows(ApiException.class, () -> TransactionTypeValidator.validate(spec));
        assertEquals("LISTING_TYPE_INVALID", ex.getErrorCode());
        assertTrue(ex.getMessage().contains("arrangementType"),
                "the message should point at the right field, not just say invalid");
    }

    @Test
    @DisplayName("an arrangement and a transaction coexist")
    void arrangementAndTransactionCoexist() {
        Map<String, Object> spec = normalised("sellingType", "Joint Venture", "listingType", "SALE");
        assertDoesNotThrow(() -> TransactionTypeValidator.validate(spec));
        assertEquals("JOINT_VENTURE", spec.get("arrangementType"));
        assertEquals("SALE", spec.get("listingType"));
    }

    @Test
    @DisplayName("failures carry what a client needs to correct the request")
    void failuresCarryDetails() {
        ApiException missing = assertThrows(ApiException.class,
                () -> TransactionTypeValidator.validate(new HashMap<>()));
        @SuppressWarnings("unchecked")
        Map<String, Object> details = (Map<String, Object>) missing.getDetails();
        assertEquals("projectSpecification.listingType", details.get("field"));
        assertEquals(java.util.List.of("SALE", "LEASE", "RENT", "AUCTION"), details.get("allowed"));

        ApiException invalid = assertThrows(ApiException.class,
                () -> TransactionTypeValidator.validate(normalised("listingType", "freehold")));
        @SuppressWarnings("unchecked")
        Map<String, Object> invalidDetails = (Map<String, Object>) invalid.getDetails();
        assertEquals("freehold", invalidDetails.get("submitted"));
    }
}
