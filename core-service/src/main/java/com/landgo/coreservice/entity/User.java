package com.landgo.coreservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity @Table(name = "users")
@Getter @Setter @SuperBuilder @NoArgsConstructor @AllArgsConstructor
public class User extends BaseEntity {
    @Column(name = "email", nullable = false, unique = true) private String email;
    @Column(name = "full_name") private String fullName;
    @Column(name = "profile_image_url") private String profileImageUrl;
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY) private VendorProfile vendorProfile;

    public boolean isVendor() { return vendorProfile != null; }
}
