package com.supporthub.knowledge.security;

import com.supporthub.knowledge.shared.identity.OrganizationId;
import com.supporthub.knowledge.shared.identity.UserId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Local authorization projection fed by organization-access events. This is not an identity store;
 * it only keeps the fields knowledge needs to authorize article and draft operations.
 */
@Entity
@Table(name = "knowledge_user_access")
@Getter
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserAccessEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "organization_id")
    private OrganizationId organizationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupportHubRole role;

    @Column(nullable = false, name = "updated_at")
    private Instant updatedAt;

    public static UserAccessEntity create(UserId userId, OrganizationId organizationId, SupportHubRole role, Instant updatedAt) {
        return UserAccessEntity.builder()
                .userId(toRaw(userId))
                .organizationId(organizationId)
                .role(role)
                .updatedAt(updatedAt)
                .build();
    }

    public void update(OrganizationId organizationId, SupportHubRole role, Instant updatedAt) {
        this.organizationId = organizationId;
        this.role = role;
        this.updatedAt = updatedAt;
    }

    public UserId getUserId() {
        return UserId.of(userId);
    }

    private static UUID toRaw(UserId userId) {
        return userId == null ? null : com.supporthub.knowledge.shared.identity.UserIdConverter.toRaw(userId);
    }
}
