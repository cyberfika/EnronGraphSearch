package br.edu.enron.model;

/**
 * Immutable record of a degree query result for a single vertex.
 *
 * <p>Used by {@code ContactAnalyzer} to return the top-20 lists for in-degree
 * and out-degree. Immutability guarantees that result objects passed around or
 * stored in collections remain consistent.</p>
 */
public final class DegreeResult implements Comparable<DegreeResult> {

    /** Normalized email address of the vertex. */
    private final String email;

    /**
     * Degree value — number of distinct in-neighbours or out-neighbours,
     * depending on the query that produced this result.
     */
    private final int degree;

    /**
     * Constructs a {@code DegreeResult}.
     *
     * @param email  the vertex email; must not be {@code null} or blank.
     * @param degree the degree value; must be non-negative.
     * @throws IllegalArgumentException if {@code email} is invalid or {@code degree} is negative.
     */
    public DegreeResult(String email, int degree) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("DegreeResult email must not be null or blank.");
        }
        if (degree < 0) {
            throw new IllegalArgumentException("Degree must not be negative.");
        }
        this.email = email;
        this.degree = degree;
    }

    /**
     * Returns the vertex email address.
     *
     * @return the email string.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the degree of the vertex for the queried direction.
     *
     * @return non-negative degree value.
     */
    public int getDegree() {
        return degree;
    }

    /**
     * Natural ordering: descending by degree, then ascending by email for tie-breaking.
     * This ordering is used directly when sorting the top-20 lists.
     *
     * @param other the result to compare against.
     * @return negative if this should appear before {@code other}.
     */
    @Override
    public int compareTo(DegreeResult other) {
        int cmp = Integer.compare(other.degree, this.degree); // descending degree
        if (cmp != 0) return cmp;
        return this.email.compareTo(other.email);             // ascending email
    }

    /**
     * Returns a formatted line suitable for console display.
     *
     * @return string in the form {@code email (degree=N)}.
     */
    @Override
    public String toString() {
        return email + " (degree=" + degree + ")";
    }
}
