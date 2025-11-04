package dev.kuku.authsome.services.tenant.impl.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity(name = "tenant_apis")
@Table(indexes = {
        @Index(columnList = "fk_tenant_id", name = "idx_tenant_api_tenant_id"),
        @Index(columnList = "key", name = "idx_tenant_api_key")
})
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TenantApiEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public String id;
    @JoinColumn(name = "fk_tenant_id", referencedColumnName = "id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    public TenantEntity tenant;
    @Column(nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.AUTO)
    public String key;
    @Column(nullable = false, name = "created_at")
    public Long createdAt;
    @Column(nullable = false, name = "updated_at")
    public Long updatedAt;
}
