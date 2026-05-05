package br.edu.enron.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Holds the data extracted from a single raw email file.
 *
 * <p>This class does <em>not</em> represent the entire raw file — only the fields
 * relevant for graph construction: the sender address and the list of recipient
 * addresses. One {@code EmailMessage} can produce multiple graph edges, one for
 * each distinct recipient.</p>
 *
 * <p>Duplicate recipients within the same message are deduplicated at construction
 * time so that a single email cannot inflate edge weights artificially.</p>
 */
public final class EmailMessage {

    /** Normalized sender email address. */
    private final String sender;

    /**
     * Deduplicated, ordered list of normalized recipient email addresses.
     * Stored as an unmodifiable view to protect internal state.
     */
    private final List<String> recipients;

    /**
     * Constructs an {@code EmailMessage} with the given sender and recipients.
     *
     * <p>Duplicate recipient addresses (after normalization) are silently removed.
     * {@code null} or blank entries in the recipients list are also discarded.</p>
     *
     * @param sender     the raw sender address; must not be {@code null} or blank.
     * @param recipients raw list of recipient addresses; must not be {@code null}.
     * @throws IllegalArgumentException if {@code sender} is {@code null} or blank,
     *                                  or if {@code recipients} is {@code null}.
     */
    public EmailMessage(String sender, List<String> recipients) {
        if (sender == null || sender.isBlank()) {
            throw new IllegalArgumentException("Sender must not be null or blank.");
        }
        if (recipients == null) {
            throw new IllegalArgumentException("Recipients list must not be null.");
        }

        this.sender = sender.trim().toLowerCase();

        // Deduplicate and filter invalid entries while preserving insertion order
        Set<String> seen = new LinkedHashSet<>();
        for (String r : recipients) {
            if (r != null && !r.isBlank()) {
                seen.add(r.trim().toLowerCase());
            }
        }
        this.recipients = List.copyOf(new ArrayList<>(seen));
    }

    /**
     * Returns the normalized sender email address.
     *
     * @return the sender address in lowercase, without surrounding whitespace.
     */
    public String getSender() {
        return sender;
    }

    /**
     * Returns an unmodifiable, deduplicated list of normalized recipient addresses.
     *
     * @return list of recipient emails; never {@code null}, may be empty.
     */
    public List<String> getRecipients() {
        return recipients;
    }

    /**
     * Determines whether this message carries enough data to produce at least one
     * graph edge: a non-blank sender and at least one recipient.
     *
     * @return {@code true} if the message is usable for graph construction.
     */
    public boolean hasValidData() {
        return !sender.isBlank() && !recipients.isEmpty();
    }

    /**
     * Returns a compact string representation for logging and debugging.
     *
     * @return formatted summary of sender and recipient count.
     */
    @Override
    public String toString() {
        return "EmailMessage{sender='" + sender + "', recipients=" + recipients.size() + "}";
    }
}
