package com.landgo.coreservice.mapper;

import com.landgo.coreservice.dto.request.SavedSearchRequest;
import com.landgo.coreservice.dto.response.SavedSearchResponse;
import com.landgo.coreservice.entity.SavedSearch;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-08T02:40:30+0530",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class SavedSearchMapperImpl implements SavedSearchMapper {

    @Override
    public SavedSearchResponse toResponse(SavedSearch savedSearch) {
        if ( savedSearch == null ) {
            return null;
        }

        SavedSearchResponse.SavedSearchResponseBuilder savedSearchResponse = SavedSearchResponse.builder();

        savedSearchResponse.city( savedSearch.getCity() );
        savedSearchResponse.createdAt( savedSearch.getCreatedAt() );
        savedSearchResponse.id( savedSearch.getId() );
        savedSearchResponse.keyword( savedSearch.getKeyword() );
        savedSearchResponse.lastNotifiedAt( savedSearch.getLastNotifiedAt() );
        savedSearchResponse.matchCount( savedSearch.getMatchCount() );
        savedSearchResponse.maxLotSize( savedSearch.getMaxLotSize() );
        savedSearchResponse.maxPrice( savedSearch.getMaxPrice() );
        savedSearchResponse.minLotSize( savedSearch.getMinLotSize() );
        savedSearchResponse.minPrice( savedSearch.getMinPrice() );
        savedSearchResponse.name( savedSearch.getName() );
        savedSearchResponse.notificationsEnabled( savedSearch.isNotificationsEnabled() );
        savedSearchResponse.projectStage( savedSearch.getProjectStage() );
        savedSearchResponse.updatedAt( savedSearch.getUpdatedAt() );

        return savedSearchResponse.build();
    }

    @Override
    public SavedSearch toEntity(SavedSearchRequest request) {
        if ( request == null ) {
            return null;
        }

        SavedSearch.SavedSearchBuilder<?, ?> savedSearch = SavedSearch.builder();

        savedSearch.city( request.getCity() );
        savedSearch.keyword( request.getKeyword() );
        savedSearch.maxLotSize( request.getMaxLotSize() );
        savedSearch.maxPrice( request.getMaxPrice() );
        savedSearch.minLotSize( request.getMinLotSize() );
        savedSearch.minPrice( request.getMinPrice() );
        savedSearch.name( request.getName() );
        savedSearch.notificationsEnabled( request.isNotificationsEnabled() );
        savedSearch.projectStage( request.getProjectStage() );

        savedSearch.matchCount( 0 );

        return savedSearch.build();
    }
}
