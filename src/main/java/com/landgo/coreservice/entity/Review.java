package com.landgo.coreservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.util.UUID;

@Entity
@Table(name = "reviews", uniqueConstraints = {@UniqueConstraint(columnNames = {"author_id", "professional_id"})})
@Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
public class Review extends BaseEntity {

    @Column(name = "professional_id", nullable = false)
    private UUID professionalId;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @ElementCollection @CollectionTable(name = "review_tags", joinColumns = @JoinColumn(name = "review_id")) @Column(name = "tag") private java.util.List<String> tags;
    @ElementCollection @CollectionTable(name = "review_photos", joinColumns = @JoinColumn(name = "review_id")) @Column(name = "photo_url") private java.util.List<String> photos;
    @Column(name = "verified_purchase") @Builder.Default private boolean verifiedPurchase = false;
}
