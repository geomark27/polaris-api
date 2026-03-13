package com.azenticsys.polaris.company.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "companies",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_companies_tax_number", columnNames = "tax_number"),
        @UniqueConstraint(name = "uq_companies_email", columnNames = "email")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String tradeName;
    
    @Column(nullable = false, length = 100)
    private String businessName;
    
    @Column(nullable = false, length = 20)
    private String taxNumber;
    
    @Column(nullable = false, length = 500)
    private String address;
    
    @Column(nullable = false, length = 20)
    private String phoneNumber;
    
    @Column(nullable = false, length = 100)
    private String email;
    
    @Column(nullable = false, length = 100)
    private String webSite;
    
    @Column(nullable = false, length = 100)
    private String logoUrl;

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
