package com.landgo.coreservice.repository;

import com.landgo.coreservice.entity.Land;
import com.landgo.coreservice.enums.ProjectStage;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
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

    public static Specification<Land> hasStatus(String status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Land> isNotDeleted() {
        return (root, query, cb) -> cb.equal(root.get("deleted"), false);
    }

    public static Specification<Land> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction();
            }
            String keywordPattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("address")), keywordPattern),
                cb.like(cb.lower(root.get("city")), keywordPattern)
            );
        };
    }

    public static Specification<Land> hasCity(String city) {
        return (root, query, cb) -> {
            if (city == null || city.trim().isEmpty()) {
                return cb.conjunction();
            }
            return cb.equal(cb.lower(root.get("city")), city.toLowerCase());
        };
    }

    public static Specification<Land> hasProjectStage(ProjectStage stage) {
        return (root, query, cb) -> {
            if (stage == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("projectStage"), stage);
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

    public static Specification<Land> hasMinLotSize(BigDecimal minLotSize) {
        return (root, query, cb) -> {
            if (minLotSize == null) {
                return cb.conjunction();
            }
            return cb.ge(root.get("lotSize"), minLotSize);
        };
    }

    public static Specification<Land> hasMaxLotSize(BigDecimal maxLotSize) {
        return (root, query, cb) -> {
            if (maxLotSize == null) {
                return cb.conjunction();
            }
            return cb.le(root.get("lotSize"), maxLotSize);
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

    public static Specification<Land> hasProjectType(String projectType) {
        return (root, query, cb) -> {
            if (projectType == null || projectType.trim().isEmpty()) return cb.conjunction();
            return cb.equal(cb.function("jsonb_extract_path_text", String.class, root.get("projectSpecification"), cb.literal("projectType")), projectType);
        };
    }

    public static Specification<Land> hasBuildingType(String buildingType) {
        return (root, query, cb) -> {
            if (buildingType == null || buildingType.trim().isEmpty()) return cb.conjunction();
            return cb.equal(cb.function("jsonb_extract_path_text", String.class, root.get("projectSpecification"), cb.literal("buildingType")), buildingType);
        };
    }

    public static Specification<Land> hasListingType(String listingType) {
        return (root, query, cb) -> {
            if (listingType == null || listingType.trim().isEmpty()) return cb.conjunction();
            return cb.equal(cb.function("jsonb_extract_path_text", String.class, root.get("projectSpecification"), cb.literal("sellingType")), listingType);
        };
    }

    public static Specification<Land> hasZoningType(String zoningType) {
        return (root, query, cb) -> {
            if (zoningType == null || zoningType.trim().isEmpty()) return cb.conjunction();
            return cb.or(
                cb.like(cb.lower(root.get("currentZoningCodes")), "%" + zoningType.toLowerCase() + "%"),
                cb.like(cb.lower(root.get("officialPlanDesignation")), "%" + zoningType.toLowerCase() + "%")
            );
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
            predicate = cb.and(predicate, cb.equal(root.get("status"), "ACTIVE"));
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