package br.edu.enron.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents an individual in the Enron contact network.
 *
 * <p>Each vertex corresponds to a unique email address. It serves as the labeled
 * node of the directed weighted graph: the label is the email itself, normalized
 * to lowercase and stripped of surrounding whitespace so that two addresses that
 * differ only in case or spacing are treated as the same person.</p>
 *
 * <p>{@link #equals(Object)} and {@link #hashCode()} are based solely on the
 * email string, which allows vertices to be safely stored in {@code HashMap} and
 * {@code HashSet} collections and looked up by address without holding a direct
 * object reference.</p>
 */
public final class Vertex implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Normalized email address that uniquely identifies this vertex. */
    private final String email;

    /**
     * Constructs a vertex for the given email address.
     *
     * @param email the raw email address; must not be {@code null} or blank.
     * @throws IllegalArgumentException if {@code email} is {@code null} or blank.
     */
    public Vertex(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Vertex email must not be null or blank.");
        }
        this.email = email.trim().toLowerCase();
    }

    /**
     * Returns the normalized email address that labels this vertex.
     *
     * @return the lowercase, trimmed email string.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Two vertices are equal if and only if their normalized email addresses are equal.
     *
     * @param obj the object to compare with.
     * @return {@code true} if {@code obj} is a {@code Vertex} with the same email.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Vertex other)) return false;
        return email.equals(other.email);
    }

    /**
     * Hash code derived exclusively from the normalized email so that the
     * contract {@code equals → same hashCode} is satisfied.
     *
     * @return hash code of the email string.
     */
    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    /**
     * Returns the email address as the string representation of this vertex.
     *
     * @return the normalized email.
     */
    @Override
    public String toString() {
        return email;
    }
}
