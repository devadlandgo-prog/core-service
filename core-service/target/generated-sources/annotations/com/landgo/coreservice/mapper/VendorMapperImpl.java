package com.landgo.coreservice.mapper;

import com.landgo.coreservice.dto.request.VendorRegisterRequest;
import com.landgo.coreservice.dto.response.VendorResponse;
import com.landgo.coreservice.entity.User;
import com.landgo.coreservice.entity.VendorProfile;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-28T19:35:17+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Homebrew)"
)
@Component
public class VendorMapperImpl implements VendorMapper {

    @Override
    public VendorResponse toResponse(VendorProfile vendor) {
        if ( vendor == null ) {
            return null;
        }

        VendorResponse.VendorResponseBuilder vendorResponse = VendorResponse.builder();

        vendorResponse.userId( vendorUserId( vendor ) );
        vendorResponse.ownerEmail( vendorUserEmail( vendor ) );
        vendorResponse.id( vendor.getId() );
        vendorResponse.companyName( vendor.getCompanyName() );
        vendorResponse.companyDescription( vendor.getCompanyDescription() );
        vendorResponse.companyLogo( vendor.getCompanyLogo() );
        vendorResponse.businessAddress( vendor.getBusinessAddress() );
        vendorResponse.businessCity( vendor.getBusinessCity() );
        vendorResponse.businessState( vendor.getBusinessState() );
        vendorResponse.businessZipCode( vendor.getBusinessZipCode() );
        vendorResponse.businessCountry( vendor.getBusinessCountry() );
        vendorResponse.website( vendor.getWebsite() );
        vendorResponse.verified( vendor.isVerified() );
        vendorResponse.rating( vendor.getRating() );
        vendorResponse.totalReviews( vendor.getTotalReviews() );
        vendorResponse.totalLandsListed( vendor.getTotalLandsListed() );
        vendorResponse.totalLandsSold( vendor.getTotalLandsSold() );
        vendorResponse.createdAt( vendor.getCreatedAt() );

        vendorResponse.ownerName( vendor.getUser().getFullName() );

        return vendorResponse.build();
    }

    @Override
    public VendorProfile toEntity(VendorRegisterRequest request) {
        if ( request == null ) {
            return null;
        }

        VendorProfile.VendorProfileBuilder<?, ?> vendorProfile = VendorProfile.builder();

        vendorProfile.companyName( request.getCompanyName() );
        vendorProfile.companyDescription( request.getCompanyDescription() );
        vendorProfile.companyLogo( request.getCompanyLogo() );
        vendorProfile.businessLicense( request.getBusinessLicense() );
        vendorProfile.businessAddress( request.getBusinessAddress() );
        vendorProfile.businessCity( request.getBusinessCity() );
        vendorProfile.businessState( request.getBusinessState() );
        vendorProfile.businessZipCode( request.getBusinessZipCode() );
        vendorProfile.businessCountry( request.getBusinessCountry() );
        vendorProfile.website( request.getWebsite() );

        vendorProfile.verified( false );
        vendorProfile.totalReviews( 0 );
        vendorProfile.totalLandsListed( 0 );
        vendorProfile.totalLandsSold( 0 );

        return vendorProfile.build();
    }

    private UUID vendorUserId(VendorProfile vendorProfile) {
        if ( vendorProfile == null ) {
            return null;
        }
        User user = vendorProfile.getUser();
        if ( user == null ) {
            return null;
        }
        UUID id = user.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String vendorUserEmail(VendorProfile vendorProfile) {
        if ( vendorProfile == null ) {
            return null;
        }
        User user = vendorProfile.getUser();
        if ( user == null ) {
            return null;
        }
        String email = user.getEmail();
        if ( email == null ) {
            return null;
        }
        return email;
    }
}
