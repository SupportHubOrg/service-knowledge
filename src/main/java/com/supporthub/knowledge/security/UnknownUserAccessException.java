package com.supporthub.knowledge.security;

import java.util.UUID;

public class UnknownUserAccessException extends AccessDeniedException {

    public UnknownUserAccessException(UUID userId) {
        super("User access is not known by knowledge service: " + userId);
    }
}
