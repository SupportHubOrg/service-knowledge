package com.supporthub.knowledge.security;

import com.supporthub.knowledge.shared.identity.OrganizationId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserAccessRepository extends JpaRepository<UserAccessEntity, UUID> {

    void deleteByOrganizationId(OrganizationId organizationId);
}
