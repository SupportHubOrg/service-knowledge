package com.supporthub.knowledge.security;

import com.supporthub.knowledge.shared.identity.OrganizationId;
import com.supporthub.knowledge.shared.identity.UserId;
import com.supporthub.knowledge.shared.identity.UserIdConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAccessProjection {

    private final UserAccessRepository repository;
    private final Clock clock;

    @Transactional
    public void upsert(UUID userId, UUID organizationId, String role) {
        upsert(userId, organizationId, SupportHubRole.fromWireValue(role));
    }

    @Transactional
    public void upsert(UUID userId, UUID organizationId, SupportHubRole role) {
        var typedUserId = UserId.of(userId);
        var typedOrganizationId = organizationId == null ? null : OrganizationId.of(organizationId);
        var updatedAt = clock.instant();
        var entity = repository.findById(UserIdConverter.toRaw(typedUserId))
                .orElseGet(() -> UserAccessEntity.create(typedUserId, typedOrganizationId, role, updatedAt));
        entity.update(typedOrganizationId, role, updatedAt);
        repository.save(entity);
    }

    @Transactional(readOnly = true)
    public Optional<UserAccessEntity> find(UUID userId) {
        return repository.findById(userId);
    }

    @Transactional
    public void removeUser(UUID userId) {
        repository.deleteById(userId);
    }

    @Transactional
    public void removeOrganization(UUID organizationId) {
        repository.deleteByOrganizationId(OrganizationId.of(organizationId));
    }
}
