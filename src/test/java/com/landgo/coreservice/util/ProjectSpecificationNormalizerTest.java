package com.landgo.coreservice.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProjectSpecificationNormalizerTest {

    private static Map<String, Object> spec(String... keyValues) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put(keyValues[i], keyValues[i + 1]);
        }
        return ProjectSpecificationNormalizer.normalize(map);
    }

    @Test
    @DisplayName("create screen building labels are stored as canonical types")
    void canonicalisesBuildingLabels() {
        assertEquals("SINGLE_FAMILY", spec("buildingType", "Detached").get("buildingType"));
        assertEquals("TOWNHOUSE", spec("buildingType", "Townhouse Block").get("buildingType"));
        assertEquals("COMMERCIAL", spec("buildingType", "Commercial Plaza").get("buildingType"));
        assertEquals("INDUSTRIAL", spec("buildingType", "Industrial Building").get("buildingType"));
    }

    @Test
    @DisplayName("an ambiguous building label is left exactly as sent")
    void leavesAmbiguousBuildingLabelAlone() {
        // "Condo / Apartment" could be CONDO or MULTI_FAMILY; choosing here would be the taxonomy
        // decision that has not been made, so the label survives and the read-side aliases find it
        // under either.
        assertEquals("Condo / Apartment", spec("buildingType", "Condo / Apartment").get("buildingType"));
    }

    @Test
    @DisplayName("an unrecognised label is kept rather than dropped or guessed at")
    void keepsUnknownLabels() {
        assertEquals("Geodesic Dome", spec("buildingType", "Geodesic Dome").get("buildingType"));
        assertEquals("Barter", spec("sellingType", "Barter").get("sellingType"));
        assertNull(spec("sellingType", "Barter").get("listingType"));
    }

    @Test
    @DisplayName("Land Sale becomes a transaction type, not an arrangement")
    void landSaleBecomesListingType() {
        Map<String, Object> result = spec("sellingType", "Land Sale");
        assertEquals("SALE", result.get("listingType"));
        assertNull(result.get("arrangementType"));
        // the submitted value survives, so nothing is lost in translation
        assertEquals("Land Sale", result.get("sellingType"));
    }

    @Test
    @DisplayName("Joint Venture and Builder Takeover become arrangements, never SALE")
    void arrangementsGetTheirOwnField() {
        Map<String, Object> jv = spec("sellingType", "Joint Venture");
        assertEquals("JOINT_VENTURE", jv.get("arrangementType"));
        assertNull(jv.get("listingType"));

        Map<String, Object> takeover = spec("sellingType", "Builder Takeover");
        assertEquals("BUILDER_TAKEOVER", takeover.get("arrangementType"));
        assertNull(takeover.get("listingType"));
    }

    @Test
    @DisplayName("an explicit listingType is never overwritten by the selling type")
    void doesNotOverwriteExplicitListingType() {
        Map<String, Object> result = spec("sellingType", "Land Sale", "listingType", "LEASE");
        assertEquals("LEASE", result.get("listingType"));
    }

    @Test
    @DisplayName("land-use categories are canonicalised in both fields that carry them")
    void canonicalisesProjectTypes() {
        assertEquals("RESIDENTIAL", spec("projectType", "Residential").get("projectType"));
        assertEquals("MIXED_USE", spec("proposedUse", "Mixed Use").get("proposedUse"));
    }

    @Test
    @DisplayName("an application-type CSV in proposedUse is left intact")
    void leavesApplicationTypeCsvAlone() {
        // Under City Submission stores "Rezoning, Site Plan" here, which is not a land-use category
        assertEquals("Rezoning, Site Plan", spec("proposedUse", "Rezoning, Site Plan").get("proposedUse"));
    }

    @Test
    @DisplayName("normalising adds nothing to an empty or unrelated spec")
    void leavesUnrelatedSpecAlone() {
        Map<String, Object> result = spec("title", "Corner lot on Main");
        assertEquals(1, result.size());
        assertFalse(result.containsKey("listingType"));
        assertNull(ProjectSpecificationNormalizer.normalize(null));
    }
}
