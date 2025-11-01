package dev.kuku.authsome.services.tenant.impl.entity;

import dev.kuku.authsome.services.tenant.api.dto.IdentityType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity(name = "tenant_identities")
@Table(
        indexes = {
                @Index(columnList = "identity_type, identity", name = "idx_tenant_identities_type_identity", unique = true),
                @Index(columnList = "fk_tenant_id", name = "idx_tenant_identities_tenant_id")
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TenantIdentityEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @JoinColumn(name = "fk_tenant_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private TenantEntity tenant;
    @Column(nullable = false, name = "identity_type")
    @Enumerated(EnumType.STRING)
    private IdentityType identityType;
    @Column(nullable = false)
    private String identity;
    @Column(nullable = false, name = "created_at")
    private Long createdAt;
    @Column(nullable = false, name = "updated_at")
    private Long updatedAt;
}
