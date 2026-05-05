package br.edu.enron.parser;

import br.edu.enron.model.EmailMessage;
import br.edu.enron.util.EmailValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses the raw text content of a single Enron email file and extracts the
 * sender and recipient addresses needed for graph construction.
 *
 * <h2>Parsing strategy</h2>
 * <p>Enron email files follow the RFC 2822 format: a header block at the top,
 * separated from the message body by the first blank line. This parser reads
 * <em>only</em> the initial header block and stops at the first blank line.
 * Any quoted or forwarded headers embedded inside the body (e.g., lines beginning
 * with {@code >}, or blocks introduced by {@code -----Original Message-----})
 * are completely ignored, because they belong to earlier messages in the thread
 * and would distort edge weights if processed.</p>
 *
 * <h2>Fields extracted</h2>
 * <ul>
 *   <li>{@code From:} — sender address (one value).</li>
 *   <li>{@code To:} — comma-separated recipient addresses.</li>
 * </ul>
 * <p>{@code Cc:} and {@code Bcc:} are deliberately excluded: the dataset's
 * {@code sent} / {@code _sent_mail} folders reflect the sender's perspective, and
 * the {@code To:} field already captures direct recipients. Including Cc/Bcc would
 * require a policy decision about whether a copy constitutes the same kind of
 * communication dependency, which is outside the project's scope.</p>
 *
 * <h2>Limitations</h2>
 * <ul>
 *   <li>Multi-line header folding (a continuation line starting with whitespace)
 *       is handled for {@code To:} by appending folded lines to the current field.</li>
 *   <li>Addresses that do not contain {@code @} are discarded by
 *       {@link EmailValidator#isValid(String)}.</li>
 * </ul>
 */
public class EmailParser {

    /**
     * Parses the raw content of one email file and returns an {@link EmailMessage}
     * containing the normalized sender and recipient list extracted from the
     * top-level header block only.
     *
     * <p>If the content is {@code null}, blank, or lacks a valid {@code From:}
     * header, the returned {@code EmailMessage} will have an empty recipient list
     * and {@link EmailMessage#hasValidData()} will return {@code false}.</p>
     *
     * @param rawContent the full text of the email file; may be {@code null}.
     * @return parsed {@link EmailMessage}; never {@code null}.
     */
    public EmailMessage parse(String rawContent) {
        if (rawContent == null || rawContent.isBlank()) {
            return new EmailMessage("unknown@unknown.com", List.of());
        }

        String sender = null;
        List<String> recipients = new ArrayList<>();

        String[] lines = rawContent.split("\n", -1);

        // Track which multi-line header we are currently accumulating
        String currentField = null;
        StringBuilder currentValue = new StringBuilder();

        for (String rawLine : lines) {
            // The first blank line marks the end of the header block — stop here.
            if (rawLine.isBlank()) break;

            boolean isFolded = rawLine.length() > 0
                    && (rawLine.charAt(0) == ' ' || rawLine.charAt(0) == '\t');

            if (isFolded && currentField != null) {
                // Continuation of the previous header field
                currentValue.append(' ').append(rawLine.trim());
                continue;
            }

            // Before starting a new field, flush the previously accumulated one
            if (currentField != null) {
                processField(currentField, currentValue.toString(), recipients);
                if ("from".equals(currentField) && sender == null) {
                    sender = extractSingleAddress(currentValue.toString());
                }
                currentField = null;
                currentValue.setLength(0);
            }

            // Identify the new field
            int colon = rawLine.indexOf(':');
            if (colon <= 0) continue;

            String fieldName = rawLine.substring(0, colon).trim().toLowerCase();
            String fieldValue = rawLine.substring(colon + 1).trim();

            if ("from".equals(fieldName) || "to".equals(fieldName)) {
                currentField = fieldName;
                currentValue.append(fieldValue);
            }
            // All other headers (Subject, Date, Message-ID, Cc, Bcc, X-*, …) are ignored
        }

        // Flush the last accumulated field
        if (currentField != null) {
            if ("from".equals(currentField) && sender == null) {
                sender = extractSingleAddress(currentValue.toString());
            }
            processField(currentField, currentValue.toString(), recipients);
        }

        if (sender == null || !EmailValidator.isValid(sender)) {
            return new EmailMessage("unknown@unknown.com", List.of());
        }

        return new EmailMessage(sender, recipients);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Appends valid recipient addresses parsed from a {@code To:} field value to
     * the running list. Does nothing for non-{@code to} fields.
     *
     * @param field     the lowercase header field name.
     * @param value     the accumulated field value (may span multiple folded lines).
     * @param recipients the list to append valid addresses to.
     */
    private void processField(String field, String value, List<String> recipients) {
        if (!"to".equals(field)) return;
        // Recipients are comma-separated; split and validate each one
        for (String part : value.split(",")) {
            String addr = EmailValidator.normalize(extractSingleAddress(part));
            if (EmailValidator.isValid(addr)) {
                recipients.add(addr);
            }
        }
    }

    /**
     * Extracts a bare email address from a potentially display-name-qualified
     * string such as {@code "John Doe <john@example.com>"} or simply
     * {@code "john@example.com"}.
     *
     * @param raw the raw address token.
     * @return the bare address, or the trimmed input if no angle brackets are found.
     */
    private String extractSingleAddress(String raw) {
        if (raw == null) return "";
        int lt = raw.indexOf('<');
        int gt = raw.indexOf('>');
        if (lt >= 0 && gt > lt) {
            return raw.substring(lt + 1, gt).trim();
        }
        return raw.trim();
    }
}
