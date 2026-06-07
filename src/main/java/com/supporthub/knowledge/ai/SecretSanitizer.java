package com.supporthub.knowledge.ai;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class SecretSanitizer {

    private static final List<SecretRule> RULES = List.of(
            new SecretRule(Pattern.compile("(?i)(authorization\\s*:\\s*bearer\\s+)([\\w.~+/-]+=*)"),
                    "$1[REDACTED_TOKEN]"),
            new SecretRule(Pattern.compile("(?i)(authorization\\s*:\\s*basic\\s+)([\\w+/=]+)"),
                    "$1[REDACTED_BASIC_AUTH]"),
            new SecretRule(Pattern.compile("(?i)(password|passwd|pwd|secret|token|api[_-]?key|client[_-]?secret)"
                    + "(\\s*[:=]\\s*)([^\\s,;\"']+)"), "$1$2[REDACTED_SECRET]"),
            new SecretRule(Pattern.compile("(?i)(password|passwd|pwd|secret|token|api[_-]?key|client[_-]?secret)"
                    + "(\\s+is\\s+)([^\\s,;\"']+)"), "$1$2[REDACTED_SECRET]"),
            new SecretRule(Pattern.compile("(?i)((?:postgres|postgresql|mysql|mongodb|redis)://[^\\s:@]+:)"
                    + "([^\\s@]+)(@[^\\s]+)"), "$1[REDACTED_PASSWORD]$3"),
            new SecretRule(Pattern.compile("(?i)(jdbc:[^\\s?]+\\?[^\\s]*?(?:password|pwd)=)([^\\s&;]+)"),
                    "$1[REDACTED_PASSWORD]"),
            new SecretRule(Pattern.compile("-----BEGIN [A-Z ]*PRIVATE KEY-----[\\s\\S]*?-----END [A-Z ]*PRIVATE KEY-----"),
                    "[REDACTED_PRIVATE_KEY]"),
            new SecretRule(Pattern.compile("\\beyJ[\\w-]+\\.[\\w-]+\\.[\\w-]+\\b"),
                    "[REDACTED_JWT]"),
            new SecretRule(Pattern.compile("\\bgh[pousr]_\\w{20,}\\b"), "[REDACTED_GITHUB_TOKEN]"),
            new SecretRule(Pattern.compile("\\bsk-[\\w-]{20,}\\b"), "[REDACTED_API_KEY]"),
            new SecretRule(Pattern.compile("\\bAKIA[A-Z\\d]{16}\\b"), "[REDACTED_AWS_ACCESS_KEY]")
    );

    private static final Pattern HIGH_ENTROPY_NEAR_KEYWORD = Pattern.compile(
            "(?i)(password|passwd|pwd|secret|token|api[_-]?key|client[_-]?secret)\\s*[:=]\\s*"
                    + "([\\w+./=-]{24,})");

    public String sanitize(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        var sanitized = text;
        for (var rule : RULES) {
            sanitized = rule.apply(sanitized);
        }
        sanitized = redactHighEntropyValues(sanitized);
        return sanitized;
    }

    public boolean containsPotentialSecret(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        if (!sanitize(text).equals(text)) {
            return true;
        }
        return HIGH_ENTROPY_NEAR_KEYWORD.matcher(text).find();
    }

    private String redactHighEntropyValues(String text) {
        var matcher = HIGH_ENTROPY_NEAR_KEYWORD.matcher(text);
        var findings = matcher.results()
                .filter(result -> isLikelyHighEntropy(result.group(2)))
                .map(result -> new Finding(result.start(2), result.end(2)))
                .sorted(Comparator.comparing(Finding::start).reversed())
                .toList();

        var builder = new StringBuilder(text);
        findings.forEach(finding -> builder.replace(finding.start(), finding.end(), "[REDACTED_SECRET]"));
        return builder.toString();
    }

    private boolean isLikelyHighEntropy(String value) {
        var hasLower = value.chars().anyMatch(Character::isLowerCase);
        var hasUpper = value.chars().anyMatch(Character::isUpperCase);
        var hasDigit = value.chars().anyMatch(Character::isDigit);
        var distinctCharacters = value.chars().distinct().count();
        return value.length() >= 24 && distinctCharacters >= 12 && (hasLower || hasUpper) && hasDigit;
    }

    private record SecretRule(Pattern pattern, String replacement) {

        String apply(String text) {
            return pattern.matcher(text).replaceAll(replacement);
        }
    }

    private record Finding(int start, int end) {
    }
}
