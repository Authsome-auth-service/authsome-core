package dev.kuku.authsome.services.tenant.impl.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity(name = "tenants")
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "identities")
@Getter
@Setter
public class TenantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, name = "password_hash")
    private String passwordHash;

    @Column(nullable = false, name = "created_at")
    private Long createdAt;

    @Column(nullable = false, name = "updated_at")
    private Long updatedAt;

    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TenantIdentityEntity> identities;
}