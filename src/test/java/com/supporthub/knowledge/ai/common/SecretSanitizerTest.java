package com.supporthub.knowledge.ai.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecretSanitizerTest {

    private final SecretSanitizer sanitizer = new SecretSanitizer();

    @Test
    void shouldRedactCommonCredentialFormats() {
        var text = """
                Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjMifQ.signature
                password=summer2026
                postgres://admin:secret123@localhost:5432/app
                jdbc:postgresql://localhost:5432/app?user=admin&password=jdbcSecret123
                token: ghp_abcdefghijklmnopqrstuvwxyz123456
                """;

        var sanitized = sanitizer.sanitize(text);

        assertThat(sanitized)
                .contains("Authorization: Bearer [REDACTED_TOKEN]")
                .contains("password=[REDACTED_SECRET]")
                .contains("postgres://admin:[REDACTED_PASSWORD]@localhost:5432/app")
                .contains("jdbc:postgresql://localhost:5432/app?user=admin&password=[REDACTED_PASSWORD]")
                .contains("token: [REDACTED_SECRET]")
                .doesNotContain("summer2026")
                .doesNotContain("secret123")
                .doesNotContain("jdbcSecret123")
                .doesNotContain("eyJhbGci");
    }

    @Test
    void shouldNotRedactNormalSupportContent() {
        var text = "Employee: VPN stopped working after password reset. Operator: Reset the VPN token in the portal.";

        assertThat(sanitizer.sanitize(text)).isEqualTo(text);
    }

    @Test
    void shouldDetectPotentialSecrets() {
        assertThat(sanitizer.containsPotentialSecret("api_key=abcDEF1234567890abcDEF1234567890")).isTrue();
        assertThat(sanitizer.containsPotentialSecret("Reset the VPN token in the portal.")).isFalse();
    }
}
