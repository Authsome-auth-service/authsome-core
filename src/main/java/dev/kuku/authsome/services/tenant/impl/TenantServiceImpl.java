package dev.kuku.authsome.services.tenant.impl;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import dev.kuku.authsome.services.tenant.api.TenantService;
import dev.kuku.authsome.services.tenant.api.dto.FetchedTenant;
import dev.kuku.authsome.services.tenant.api.dto.FetchedTenantIdentity;
import dev.kuku.authsome.services.tenant.api.dto.IdentityType;
import dev.kuku.authsome.services.tenant.api.dto.TenantAndRefreshToken;
import dev.kuku.authsome.services.tenant.impl.entity.TenantEntity;
import dev.kuku.authsome.services.tenant.impl.entity.TenantIdentityEntity;
import dev.kuku.authsome.services.tenant.impl.entity.TenantSessionEntity;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

import static dev.kuku.authsome.util.Util.NowUTCMilli;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantServiceImpl implements TenantService {

    private final EntityManager entityManager;
    private final CriteriaBuilderFactory cbf;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${authsome.tenant.max-simultaneous-sessions:5}")
    int maxSimultaneousSessions;

    // ===========================================================
    // Tenant Retrieval
    // ===========================================================
    @Override
    public FetchedTenant getTenantByIdentity(IdentityType identityType, String identity) {
        log.debug("getTenantByIdentity({}, {})", identityType, identity);
        CriteriaBuilder<TenantEntity> cb = cbf.create(entityManager, TenantEntity.class);

        TenantEntity tenant = cb.from(TenantIdentityEntity.class, "ti")
                .select("ti.tenant")
                .where("ti.identityType").eq(identityType)
                .where("ti.identity").eq(identity)
                .getSingleResultOrNull();

        return convert(tenant);
    }

    @Override
    public FetchedTenant getTenantByUsername(String username) {
        log.debug("getTenantByUsername({})", username);
        var cb = cbf.create(entityManager, TenantEntity.class);
        cb.from(TenantEntity.class, "t")
                .where("t.username").eq(username);
        TenantEntity tenant = cb.getSingleResultOrNull();
        return convert(tenant);
    }

    // ===========================================================
    // Tenant Creation
    // ===========================================================
    @Transactional
    @Override
    public FetchedTenant createTenant(String username, String rawPassword) {
        log.debug("createTenant({}, ****)", username);

        TenantEntity tenant = new TenantEntity();
        tenant.setUsername(username);
        tenant.setPasswordHash(passwordEncoder.encode(rawPassword));
        tenant.setCreatedAt(NowUTCMilli());
        tenant.setUpdatedAt(NowUTCMilli());

        entityManager.persist(tenant);
        entityManager.flush();

        return convert(tenant);
    }

    // ===========================================================
    // Tenant Identities
    // ===========================================================
    @Transactional
    @Override
    public FetchedTenantIdentity addIdentityForTenant(String tenantId, IdentityType identityType, String identity) {
        log.debug("addIdentityForTenant({}, {}, {})", tenantId, identityType, identity);

        // Check if identity already exists
        CriteriaBuilder<TenantIdentityEntity> cb = cbf.create(entityManager, TenantIdentityEntity.class);
        cb.from(TenantIdentityEntity.class, "ti")
                .where("ti.identityType").eq(identityType)
                .where("ti.identity").eq(identity);
        TenantIdentityEntity existing = cb.getSingleResultOrNull();
        if (existing != null) {
            throw new IllegalArgumentException("Identity already taken");
        }

        TenantIdentityEntity tenantIdentity = new TenantIdentityEntity();
        tenantIdentity.setIdentity(identity);
        tenantIdentity.setIdentityType(identityType);
        tenantIdentity.setTenant(entityManager.getReference(TenantEntity.class, UUID.fromString(tenantId)));
        tenantIdentity.setCreatedAt(NowUTCMilli());
        tenantIdentity.setUpdatedAt(NowUTCMilli());

        entityManager.persist(tenantIdentity);
        entityManager.flush();

        return convert(tenantIdentity);
    }

    // ===========================================================
    // Tenant Authentication
    // ===========================================================
    @Override
    public boolean validateTenantCredentials(String tenantId, String rawPassword) {
        log.debug("validateTenantCredentials({}, ****)", tenantId);
        TenantEntity tenant = entityManager.find(TenantEntity.class, UUID.fromString(tenantId));
        return tenant != null && passwordEncoder.matches(rawPassword, tenant.getPasswordHash());
    }

    // ===========================================================
    // Refresh Tokens
    // ===========================================================
    @Transactional
    @Override
    public String createTenantRefreshToken(String tenantId, Map<String, Object> metadata) {
        log.debug("createTenantRefreshToken({}, metadata size={})", tenantId,
                metadata != null ? metadata.size() : 0);

        UUID tenantUUID = UUID.fromString(tenantId);
        long now = NowUTCMilli();

        // Delete expired sessions for this tenant
        cbf.delete(entityManager, TenantSessionEntity.class)
                .where("fk_tenant_id").eq(tenantUUID)
                .where("expiresAt").lt(now)
                .executeUpdate();

        // Check active session count
        var cb = cbf.create(entityManager, Long.class);
        cb.from(TenantSessionEntity.class, "s")
                .where("s.fk_tenant_id").eq(tenantUUID)
                .select("COUNT(s.id)");
        Long sessionCount = cb.getSingleResult();
        if (sessionCount >= maxSimultaneousSessions) {
            throw new IllegalStateException("Max simultaneous sessions reached");
        }

        // Create new session
        TenantSessionEntity session = new TenantSessionEntity();
        session.setTenant(entityManager.getReference(TenantEntity.class, tenantUUID));
        session.setCreatedAt(now);
        session.setUpdatedAt(now);
        session.setExpiresAt(now + 30L * 24 * 60 * 60 * 1000); // 30 days
        session.setMetadata(metadata);

        entityManager.persist(session);
        entityManager.flush();

        return session.getId().toString();
    }

    @Transactional
    @Override
    public TenantAndRefreshToken refreshToken(String refreshToken) {
        log.debug("refreshToken({})", refreshToken);
        UUID sessionId = UUID.fromString(refreshToken);
        long now = NowUTCMilli();

        TenantSessionEntity session = cbf.create(entityManager, TenantSessionEntity.class)
                .where("id").eq(sessionId)
                .getSingleResultOrNull();

        if (session == null) {
            log.error("refreshToken({}) not found", refreshToken);
            return null;
        }

        if (session.getExpiresAt() <= now) {
            log.info("refreshToken({}) expired, cleaning up", refreshToken);
            cbf.delete(entityManager, TenantSessionEntity.class)
                    .where("id").eq(sessionId)
                    .executeUpdate();
            return null;
        }

        // Delete the existing session
        cbf.delete(entityManager, TenantSessionEntity.class)
                .where("id").eq(sessionId)
                .executeUpdate();

        TenantEntity tenant = session.getTenant();
        String newRt = createTenantRefreshToken(tenant.getId().toString(), session.getMetadata());

        return new TenantAndRefreshToken(convert(tenant), newRt);
    }

    @Transactional
    @Override
    public void revokeTenantRefreshToken(String refreshToken) {
        log.debug("revokeTenantRefreshToken({})", refreshToken);
        int deleted = cbf.delete(entityManager, TenantSessionEntity.class)
                .where("id").eq(UUID.fromString(refreshToken))
                .executeUpdate();

        if (deleted == 0) {
            log.warn("No session found to revoke for refreshToken({})", refreshToken);
        }
    }

    // ===========================================================
    // Converters
    // ===========================================================
    private FetchedTenant convert(TenantEntity tenantEntity) {
        if (tenantEntity == null) return null;
        return new FetchedTenant(
                tenantEntity.getId().toString(),
                tenantEntity.getUsername(),
                tenantEntity.getCreatedAt(),
                tenantEntity.getUpdatedAt()
        );
    }

    private FetchedTenantIdentity convert(TenantIdentityEntity tenantIdentityEntity) {
        if (tenantIdentityEntity == null) return null;
        return new FetchedTenantIdentity(
                tenantIdentityEntity.getId().toString(),
                tenantIdentityEntity.getTenant().getId().toString(),
                tenantIdentityEntity.getIdentityType(),
                tenantIdentityEntity.getIdentity()
        );
    }
}
