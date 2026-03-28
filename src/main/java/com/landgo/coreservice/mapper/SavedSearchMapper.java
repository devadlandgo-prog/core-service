package com.landgo.coreservice.mapper;
import com.landgo.coreservice.dto.request.SavedSearchRequest;
import com.landgo.coreservice.dto.response.SavedSearchResponse;
import com.landgo.coreservice.entity.SavedSearch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
@Mapper(componentModel = "spring")
public interface SavedSearchMapper {
    SavedSearchResponse toResponse(SavedSearch savedSearch);
    @Mapping(target = "id", ignore = true) @Mapping(target = "createdAt", ignore = true) @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true) @Mapping(target = "user", ignore = true)
    @Mapping(target = "matchCount", constant = "0") @Mapping(target = "lastNotifiedAt", ignore = true)
    SavedSearch toEntity(SavedSearchRequest request);
}
