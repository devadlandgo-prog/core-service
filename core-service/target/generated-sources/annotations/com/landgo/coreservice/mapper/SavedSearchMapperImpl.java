package com.landgo.coreservice.mapper;

import com.landgo.coreservice.dto.request.SavedSearchRequest;
import com.landgo.coreservice.dto.response.SavedSearchResponse;
import com.landgo.coreservice.entity.SavedSearch;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-28T19:35:17+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Homebrew)"
)
@Component
public class SavedSearchMapperImpl implements SavedSearchMapper {

    @Override
    public SavedSearchResponse toResponse(SavedSearch savedSearch) {
        if ( savedSearch == null ) {
            return null;
        }

        SavedSearchResponse.SavedSearchResponseBuilder savedSearchResponse = SavedSearchResponse.builder();

        savedSearchResponse.id( savedSearch.getId() );
        savedSearchResponse.name( savedSearch.getName() );
        savedSearchResponse.keyword( savedSearch.getKeyword() );
        savedSearchResponse.city( savedSearch.getCity() );
        savedSearchResponse.projectStage( savedSearch.getProjectStage() );
        savedSearchResponse.minPrice( savedSearch.getMinPrice() );
        savedSearchResponse.maxPrice( savedSearch.getMaxPrice() );
        savedSearchResponse.minLotSize( savedSearch.getMinLotSize() );
        savedSearchResponse.maxLotSize( savedSearch.getMaxLotSize() );
        savedSearchResponse.notificationsEnabled( savedSearch.isNotificationsEnabled() );
        savedSearchResponse.matchCount( savedSearch.getMatchCount() );
        savedSearchResponse.lastNotifiedAt( savedSearch.getLastNotifiedAt() );
        savedSearchResponse.createdAt( savedSearch.getCreatedAt() );
        savedSearchResponse.updatedAt( savedSearch.getUpdatedAt() );

        return savedSearchResponse.build();
    }

    @Override
    public SavedSearch toEntity(SavedSearchRequest request) {
        if ( request == null ) {
            return null;
        }

        SavedSearch.SavedSearchBuilder<?, ?> savedSearch = SavedSearch.builder();

        savedSearch.name( request.getName() );
        savedSearch.keyword( request.getKeyword() );
        savedSearch.city( request.getCity() );
        savedSearch.projectStage( request.getProjectStage() );
        savedSearch.minPrice( request.getMinPrice() );
        savedSearch.maxPrice( request.getMaxPrice() );
        savedSearch.minLotSize( request.getMinLotSize() );
        savedSearch.maxLotSize( request.getMaxLotSize() );
        savedSearch.notificationsEnabled( request.isNotificationsEnabled() );

        savedSearch.matchCount( 0 );

        return savedSearch.build();
    }
}
