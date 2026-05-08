package com.landgo.coreservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "locations")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Location extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "code", nullable = false, length = 10)
    private String code;

    @Column(name = "type", nullable = false, length = 20)
    private String type; // COUNTRY, STATE, CITY

    @Column(name = "parent_id")
    private java.util.UUID parentId;

    @Column(name = "is_active")
    @Builder.Default
    private boolean isActive = true;
}
