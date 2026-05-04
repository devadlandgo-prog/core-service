package com.landgo.coreservice.repository;

import com.landgo.coreservice.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LocationRepository extends JpaRepository<Location, UUID> {
    List<Location> findByTypeAndIsActiveTrue(String type);
    List<Location> findByParentIdAndIsActiveTrue(UUID parentId);
    java.util.Optional<Location> findByCodeAndTypeAndIsActiveTrue(String code, String type);
}
