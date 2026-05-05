package br.edu.enron.util;

/**
 * Utility class for normalizing and validating email addresses.
 *
 * <p>Validation here is intentionally minimal: the Enron dataset contains many
 * non-standard addresses (internal aliases, distribution lists, etc.), so strict
 * RFC 5321 validation would reject legitimate entries. The only hard requirement
 * is the presence of {@code @} and a non-empty string on each side.</p>
 */
public final class EmailValidator {

    private EmailValidator() {
        // utility class — no instances
    }

    /**
     * Normalizes an email address by trimming surrounding whitespace and converting
     * to lowercase.
     *
     * @param email the raw address; may be {@code null}.
     * @return normalized address, or an empty string if {@code email} is {@code null}.
     */
    public static String normalize(String email) {
        if (email == null) return "";
        return email.trim().toLowerCase();
    }

    /**
     * Returns {@code true} if the given string is a usable email address for graph
     * construction purposes: non-null, non-blank, and containing exactly one
     * {@code @} character with non-empty local and domain parts.
     *
     * @param email the address to validate (raw or already normalized).
     * @return {@code true} if the address can be used as a vertex label.
     */
    public static boolean isValid(String email) {
        if (email == null || email.isBlank()) return false;
        String trimmed = email.trim();
        int atIndex = trimmed.indexOf('@');
        if (atIndex <= 0) return false;                      // no @ or nothing before it
        if (atIndex == trimmed.length() - 1) return false;  // nothing after @
        return trimmed.indexOf('@', atIndex + 1) == -1;     // only one @
    }
}
