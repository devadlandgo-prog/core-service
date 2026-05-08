package com.landgo.coreservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorListingStatsResponse {
    private long activeListings;
    private long totalListings;
    private long totalViews;
    private long totalEnquiries;
}
