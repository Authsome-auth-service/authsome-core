package dev.kuku.authsome.services.tenant.impl.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;
import java.util.UUID;

@Entity(name = "tenant_sessions")
@Table(indexes = {@Index(columnList = "fk_tenant_id", name = "idx_tenant_sessions_fk_tenant_id"), @Index(columnList = "expires_at", name = "idx_tenant_sessions_expires")})
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class TenantSessionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @JoinColumn(name = "fk_tenant_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private TenantEntity tenant;
    @Column(nullable = false, name = "expires_at")
    private Long expiresAt;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata")
    private Map<String, Object> metadata;
    @Column(nullable = false, name = "created_at")
    private Long createdAt;
    @Column(nullable = false, name = "updated_at")
    private Long updatedAt;
}
