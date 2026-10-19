package com.landgo.coreservice.repository;

import com.landgo.coreservice.entity.Land;
import com.landgo.coreservice.enums.ArrangementType;
import com.landgo.coreservice.enums.ProjectStage;
import com.landgo.coreservice.enums.ProjectType;
import com.landgo.coreservice.enums.BuildingType;
import com.landgo.coreservice.enums.ZoningType;
import com.landgo.coreservice.enums.ListingType;
import com.landgo.coreservice.enums.LandStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class LandSpecification {

    public static Specification<Land> searchLands(String search) {
        return (Root<Land> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (search == null || search.trim().isEmpty()) {
                return cb.conjunction(); // always true
            }
            String searchPattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("address")), searchPattern),
                cb.like(cb.lower(root.get("city")), searchPattern)
            );
        };
    }

    public static Specification<Land> hasStatus(LandStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Land> isNotDeleted() {
        return (root, query, cb) -> cb.equal(root.get("deleted"), false);
    }

    public static Specification<Land> hasStatusesAndSoldSince(java.util.List<LandStatus> statuses, Integer soldSinceDays) {
        return (root, query, cb) -> {
            java.util.List<LandStatus> queryStatuses = statuses;
            if (queryStatuses == null || queryStatuses.isEmpty()) {
                queryStatuses = java.util.List.of(LandStatus.ACTIVE, LandStatus.SOLD);
            }
            
            queryStatuses = queryStatuses.stream()
                    .filter(s -> s == LandStatus.ACTIVE || s == LandStatus.SOLD)
                    .toList();
            
            if (queryStatuses.isEmpty()) {
                return cb.disjunction();
            }
            
            java.util.List<Predicate> predicates = new java.util.ArrayList<>();
            for (LandStatus status : queryStatuses) {
                if (status == LandStatus.SOLD && soldSinceDays != null) {
                    java.time.LocalDateTime since = java.time.LocalDateTime.now().minusDays(soldSinceDays);
                    predicates.add(cb.and(
                        cb.equal(root.get("status"), LandStatus.SOLD),
                        cb.greaterThanOrEqualTo(root.get("updatedAt"), since)
                    ));
                } else {
                    predicates.add(cb.equal(root.get("status"), status));
                }
            }
            
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Land> excludeSoldUnlessSpecified(Integer soldSinceDays) {
        return (root, query, cb) -> {
            if (soldSinceDays != null) {
                // If soldSinceDays is provided, we specifically want SOLD properties within that timeframe.
                java.time.LocalDateTime since = java.time.LocalDateTime.now().minusDays(soldSinceDays);
                return cb.and(
                    cb.equal(root.get("status"), LandStatus.SOLD),
                    cb.greaterThanOrEqualTo(root.get("updatedAt"), since)
                );
            } else {
                // Otherwise return public listings (excluding SOLD, PENDING_APPROVAL, and REJECTED)
                return cb.and(
                    cb.notEqual(root.get("status"), LandStatus.SOLD),
                    cb.notEqual(root.get("status"), LandStatus.PENDING_APPROVAL),
                    cb.notEqual(root.get("status"), LandStatus.REJECTED)
                );
            }
        };
    }

    public static Specification<Land> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction();
            }
            String keywordPattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("address")), keywordPattern),
                cb.like(cb.lower(root.get("city")), keywordPattern),
                cb.like(cb.lower(cb.function("jsonb_extract_path_text", String.class,
                        root.get("projectSpecification"), cb.literal("title"))), keywordPattern)
            );
        };
    }

    public static Specification<Land> hasCity(String city) {
        return (root, query, cb) -> {
            if (city == null || city.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.equal(cb.lower(root.get("city")), city.trim().toLowerCase());
        };
    }

    public static Specification<Land> hasProjectStages(java.util.List<ProjectStage> stages) {
        return (root, query, cb) -> {
            if (stages == null || stages.isEmpty()) {
                return cb.conjunction();
            }
            return root.get("projectStage").in(stages);
        };
    }

    public static Specification<Land> hasMinPrice(BigDecimal minPrice) {
        return (root, query, cb) -> {
            if (minPrice == null) {
                return cb.conjunction();
            }
            return cb.ge(root.get("askingPrice"), minPrice);
        };
    }

    public static Specification<Land> hasMaxPrice(BigDecimal maxPrice) {
        return (root, query, cb) -> {
            if (maxPrice == null) {
                return cb.conjunction();
            }
            return cb.le(root.get("askingPrice"), maxPrice);
        };
    }

    /**
     * Lower bound in acres.
     *
     * <p>Compares the generated acre column, not the raw lot_size: the bounds come from an acreage
     * input, while listings are stored in whatever unit the seller chose, so comparing the raw
     * number made "1 to 2 acres" exclude a lot entered as 43560 SQFT and include one of 43560
     * acres.
     */
    public static Specification<Land> hasMinLotSize(BigDecimal minLotSize) {
        return (root, query, cb) -> {
            if (minLotSize == null) {
                return cb.conjunction();
            }
            return cb.ge(root.get("lotSizeAcres"), minLotSize);
        };
    }

    public static Specification<Land> hasMaxLotSize(BigDecimal maxLotSize) {
        return (root, query, cb) -> {
            if (maxLotSize == null) {
                return cb.conjunction();
            }
            return cb.le(root.get("lotSizeAcres"), maxLotSize);
        };
    }

    public static Specification<Land> isFeatured(Boolean featured) {
        return (root, query, cb) -> {
            if (featured == null) return cb.conjunction();
            return cb.equal(root.get("isFeatured"), featured);
        };
    }

    public static Specification<Land> isHotDeal(Boolean hotDeal) {
        return (root, query, cb) -> {
            if (hotDeal == null) return cb.conjunction();
            return cb.equal(root.get("isHotDeal"), hotDeal);
        };
    }

    /**
     * A {@code projectSpecification} field, lower-cased with spaces and hyphens as underscores, so a
     * stored display label can be compared against a canonical token.
     */
    private static Expression<String> normalizedSpecField(jakarta.persistence.criteria.Root<Land> root,
            CriteriaBuilder cb, String field) {
        return cb.lower(cb.function("replace", String.class,
                cb.function("replace", String.class,
                        cb.function("jsonb_extract_path_text", String.class,
                                root.get("projectSpecification"), cb.literal(field)),
                        cb.literal(" "), cb.literal("_")),
                cb.literal("-"), cb.literal("_")));
    }

    /**
     * Land-use category.
     *
     * <p>Matched against {@code proposedUse} as well as {@code projectType} because the Site Plan
     * Approval create step writes the category to {@code proposedUse} and leaves {@code projectType}
     * unset - the response mapper already reads it that way, so a filter that looked only at
     * {@code projectType} disagreed with the category the listing displays. Application labels that
     * share the field on other stages ("Rezoning, Site Plan") match no category token, so widening
     * the field list adds no false positives.
     */
    public static Specification<Land> hasProjectTypes(java.util.List<ProjectType> projectTypes) {
        return (root, query, cb) -> {
            if (projectTypes == null || projectTypes.isEmpty()) return cb.conjunction();
            java.util.List<String> tokens = projectTypes.stream()
                    .map(pt -> pt.name().toLowerCase())
                    .toList();
            return cb.or(
                    normalizedSpecField(root, cb, "projectType").in(tokens),
                    normalizedSpecField(root, cb, "proposedUse").in(tokens));
        };
    }

    public static Specification<Land> hasBuildingTypes(java.util.List<BuildingType> buildingTypes) {
        return (root, query, cb) -> {
            if (buildingTypes == null || buildingTypes.isEmpty()) return cb.conjunction();
            // matchTokens() rather than name(): the create screens store display labels, so the rows
            // say "Detached" where the filter asks for single_family.
            java.util.Set<String> tokens = new java.util.LinkedHashSet<>();
            buildingTypes.forEach(bt -> tokens.addAll(bt.matchTokens()));
            return normalizedSpecField(root, cb, "buildingType").in(tokens);
        };
    }

    public static Specification<Land> hasListingTypes(java.util.List<ListingType> listingTypes) {
        return (root, query, cb) -> {
            if (listingTypes == null || listingTypes.isEmpty()) return cb.conjunction();
            java.util.Set<String> tokens = new java.util.LinkedHashSet<>();
            listingTypes.forEach(lt -> tokens.addAll(lt.matchTokens()));
            // sellingType too: create writes the transaction there ("Land Sale") and leaves
            // listingType unset for every stage that has no transaction picker.
            return cb.or(
                    normalizedSpecField(root, cb, "listingType").in(tokens),
                    normalizedSpecField(root, cb, "sellingType").in(tokens));
        };
    }

    public static Specification<Land> hasZoningTypes(java.util.List<ZoningType> zoningTypes) {
        return (root, query, cb) -> {
            if (zoningTypes == null || zoningTypes.isEmpty()) return cb.conjunction();
            
            Predicate[] predicates = zoningTypes.stream().map(zt -> {
                String searchStr = "%" + zt.name().toLowerCase() + "%";
                return cb.or(
                    cb.like(cb.lower(root.get("currentZoningCodes")), searchStr),
                    cb.like(cb.lower(root.get("officialPlanDesignation")), searchStr)
                );
            }).toArray(Predicate[]::new);
            
            // Should match AT LEAST ONE of the provided zoning types (OR)
            return cb.or(predicates);
        };
    }

    /**
     * Commercial arrangement - joint venture, builder takeover - as opposed to the transaction.
     *
     * <p>Reads {@code sellingType} alongside the canonical {@code arrangementType} because every
     * listing stored before the split put the arrangement in the selling type, which is also where
     * the create screen still sends it.
     */
    public static Specification<Land> hasArrangementTypes(java.util.List<ArrangementType> arrangementTypes) {
        return (root, query, cb) -> {
            if (arrangementTypes == null || arrangementTypes.isEmpty()) return cb.conjunction();
            java.util.Set<String> tokens = new java.util.LinkedHashSet<>();
            arrangementTypes.forEach(at -> tokens.addAll(at.matchTokens()));
            return cb.or(
                    normalizedSpecField(root, cb, "arrangementType").in(tokens),
                    normalizedSpecField(root, cb, "sellingType").in(tokens));
        };
    }

    /**
     * Municipal zoning code search, e.g. {@code R1}, {@code RM4}.
     *
     * <p>Separate from {@link #hasZoningTypes} because a code and a category are different things:
     * the zoning filter advertised "e.g. Residential, R1, Commercial" but sent everything as
     * {@code zoningType}, where {@code R1} is not a valid category and the request failed outright.
     * Codes are what creation actually collects, so they need a parameter of their own.
     *
     * <p>A prefix-ish contains match, since a row holds a comma-separated list of codes and callers
     * type one.
     */
    public static Specification<Land> hasZoningCodes(java.util.List<String> zoningCodes) {
        return (root, query, cb) -> {
            if (zoningCodes == null || zoningCodes.isEmpty()) return cb.conjunction();
            Predicate[] predicates = zoningCodes.stream()
                    .filter(code -> code != null && !code.isBlank())
                    .map(code -> cb.like(cb.lower(root.get("currentZoningCodes")),
                            "%" + code.trim().toLowerCase() + "%"))
                    .toArray(Predicate[]::new);
            return predicates.length == 0 ? cb.conjunction() : cb.or(predicates);
        };
    }

    public static Specification<Land> forSaleSince(Integer days) {
        return (root, query, cb) -> {
            if (days == null) return cb.conjunction();
            java.time.LocalDateTime since = java.time.LocalDateTime.now().minusDays(days);
            return cb.greaterThanOrEqualTo(root.get("createdAt"), since);
        };
    }

    public static Specification<Land> forSavedSearchCriteria(String keyword, String city, ProjectStage stage,
                                                         BigDecimal minPrice, BigDecimal maxPrice,
                                                         BigDecimal minLotSize, BigDecimal maxLotSize) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();
            predicate = cb.and(predicate, cb.equal(root.get("status"), LandStatus.ACTIVE));
            predicate = cb.and(predicate, cb.equal(root.get("deleted"), false));

            if (keyword != null && !keyword.trim().isEmpty()) {
                String keywordPattern = "%" + keyword.toLowerCase() + "%";
                predicate = cb.and(predicate, cb.or(
                    cb.like(cb.lower(root.get("address")), keywordPattern),
                    cb.like(cb.lower(root.get("city")), keywordPattern)
                ));
            }

            if (city != null && !city.trim().isEmpty()) {
                predicate = cb.and(predicate, cb.equal(cb.lower(root.get("city")), city.toLowerCase()));
            }

            if (stage != null) {
                predicate = cb.and(predicate, cb.equal(root.get("projectStage"), stage));
            }

            if (minPrice != null) {
                predicate = cb.and(predicate, cb.ge(root.get("askingPrice"), minPrice));
            }

            if (maxPrice != null) {
                predicate = cb.and(predicate, cb.le(root.get("askingPrice"), maxPrice));
            }

            if (minLotSize != null) {
                predicate = cb.and(predicate, cb.ge(root.get("lotSize"), minLotSize));
            }

            if (maxLotSize != null) {
                predicate = cb.and(predicate, cb.le(root.get("lotSize"), maxLotSize));
            }

            return predicate;
        };
    }
}