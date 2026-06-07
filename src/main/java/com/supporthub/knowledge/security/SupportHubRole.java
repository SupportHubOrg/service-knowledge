package com.supporthub.knowledge.security;

public enum SupportHubRole {
    EMPLOYEE,
    AI,
    LEVEL_1_OPERATOR,
    LEVEL_2_OPERATOR,
    ORGANIZATION_MANAGER,
    SUPPORT_HUB_ADMINISTRATOR,
    SYSTEM

    ;

    public static SupportHubRole fromWireValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Role is blank");
        }
        return switch (normalize(value)) {
            case "EMPLOYEE" -> EMPLOYEE;
            case "AI" -> AI;
            case "LEVEL_1_OPERATOR", "OPERATOR_LEVEL_1" -> LEVEL_1_OPERATOR;
            case "LEVEL_2_OPERATOR", "OPERATOR_LEVEL_2" -> LEVEL_2_OPERATOR;
            case "ORGANIZATION_MANAGER" -> ORGANIZATION_MANAGER;
            case "SUPPORT_HUB_ADMINISTRATOR", "SUPPORT_HUB_ADMIN" -> SUPPORT_HUB_ADMINISTRATOR;
            case "SYSTEM" -> SYSTEM;
            default -> throw new IllegalArgumentException("Unsupported SupportHub role: " + value);
        };
    }

    private static String normalize(String value) {
        return value.trim()
                .replace('-', '_')
                .replace(' ', '_')
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .replaceAll("([A-Za-z])(\\d)", "$1_$2")
                .toUpperCase()
                .replace("ROLE_", "");
    }
}
