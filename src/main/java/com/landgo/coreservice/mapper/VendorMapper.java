package com.landgo.coreservice.mapper;
import com.landgo.coreservice.dto.request.VendorRegisterRequest;
import com.landgo.coreservice.dto.response.VendorResponse;
import com.landgo.coreservice.entity.VendorProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
@Mapper(componentModel = "spring")
public interface VendorMapper {
    @Mapping(target = "ownerName", ignore = true)
    @Mapping(target = "ownerEmail", ignore = true)
    VendorResponse toResponse(VendorProfile vendor);

    @Mapping(target = "id", ignore = true) @Mapping(target = "createdAt", ignore = true) @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "verified", constant = "false") @Mapping(target = "rating", ignore = true)
    @Mapping(target = "totalReviews", constant = "0") @Mapping(target = "totalLandsListed", constant = "0")
    @Mapping(target = "totalLandsSold", constant = "0")
    VendorProfile toEntity(VendorRegisterRequest request);

    @Mapping(target = "id", ignore = true) @Mapping(target = "createdAt", ignore = true) @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "verified", ignore = true) @Mapping(target = "rating", ignore = true)
    @Mapping(target = "totalReviews", ignore = true) @Mapping(target = "totalLandsListed", ignore = true)
    @Mapping(target = "totalLandsSold", ignore = true)
    void updateEntity(VendorRegisterRequest request, @org.mapstruct.MappingTarget VendorProfile vendor);
}
