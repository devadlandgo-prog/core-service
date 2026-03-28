package com.landgo.coreservice.entity;

import com.landgo.coreservice.enums.DraftStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.Map;

@Entity @Table(name = "listing_drafts")
@Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
public class ListingDraft extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "owner_id", nullable = false) private User owner;
    @Enumerated(EnumType.STRING) @Column(name = "status", nullable = false, length = 20) @Builder.Default private DraftStatus status = DraftStatus.IN_PROGRESS;
    @Column(name = "current_step") @Builder.Default private Integer currentStep = 0;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "step1_data", columnDefinition = "jsonb") private Map<String, Object> step1Data;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "step2_data", columnDefinition = "jsonb") private Map<String, Object> step2Data;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "step3_data", columnDefinition = "jsonb") private Map<String, Object> step3Data;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "step4_data", columnDefinition = "jsonb") private Map<String, Object> step4Data;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "step5_data", columnDefinition = "jsonb") private Map<String, Object> step5Data;
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "published_listing_id") private Land publishedListing;
}
