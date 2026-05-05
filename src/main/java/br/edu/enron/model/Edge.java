package br.edu.enron.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a directed, weighted edge between two individuals in the contact graph.
 *
 * <p>An edge from {@code origin} to {@code destination} is created the first time
 * {@code origin} sends a message to {@code destination}. Each subsequent message
 * between the same pair increments the {@link #weight} by one via
 * {@link #incrementWeight()}, so the weight always reflects the total number of
 * messages sent from origin to destination.</p>
 *
 * <p>The edge is directional: an edge A→B is entirely independent of an edge B→A.
 * This matches the semantics of the Enron dataset where sending and receiving are
 * asymmetric roles.</p>
 *
 * <p>The method {@link #getInverseCost()} returns {@code 1.0 / weight} and is used
 * by the adapted Dijkstra algorithm in {@code CriticalPathService}: a high-weight
 * edge (strong communication dependency) is mapped to a low cost, so the shortest
 * path through inverse costs corresponds to the path of maximum dependency.</p>
 */
public final class Edge implements Serializable {

    private static final long serialVersionUID = 1L;

    /** The sender vertex — source of the directed edge. */
    private final Vertex origin;

    /** The recipient vertex — target of the directed edge. */
    private final Vertex destination;

    /**
     * Number of messages sent from {@link #origin} to {@link #destination}.
     * Starts at 1 and grows via {@link #incrementWeight()}.
     */
    private int weight;

    /**
     * Constructs a new edge with an initial weight of 1.
     *
     * @param origin      the sending vertex; must not be {@code null}.
     * @param destination the receiving vertex; must not be {@code null}.
     * @throws IllegalArgumentException if either vertex is {@code null}.
     */
    public Edge(Vertex origin, Vertex destination) {
        if (origin == null || destination == null) {
            throw new IllegalArgumentException("Edge origin and destination must not be null.");
        }
        this.origin = origin;
        this.destination = destination;
        this.weight = 1;
    }

    /**
     * Returns the origin (sender) vertex of this directed edge.
     *
     * @return the sending vertex.
     */
    public Vertex getOrigin() {
        return origin;
    }

    /**
     * Returns the destination (recipient) vertex of this directed edge.
     *
     * @return the receiving vertex.
     */
    public Vertex getDestination() {
        return destination;
    }

    /**
     * Returns the current weight of this edge, i.e., the total number of messages
     * sent from {@link #origin} to {@link #destination}.
     *
     * @return a positive integer representing message frequency.
     */
    public int getWeight() {
        return weight;
    }

    /**
     * Increments the edge weight by one, recording one additional message sent
     * from {@link #origin} to {@link #destination}.
     */
    public void incrementWeight() {
        this.weight++;
    }

    /**
     * Returns the inverse cost used by the adapted Dijkstra algorithm.
     *
     * <p>Formula: {@code 1.0 / weight}. A heavier edge (more messages) produces a
     * smaller cost, so Dijkstra minimising this value will favour paths that pass
     * through high-frequency communication links — yielding the critical
     * (maximum-dependency) path.</p>
     *
     * @return {@code 1.0 / weight}, always a positive double.
     */
    public double getInverseCost() {
        return 1.0 / weight;
    }

    /**
     * Returns a human-readable representation of the edge, including origin,
     * destination and current weight.
     *
     * @return formatted edge string.
     */
    @Override
    public String toString() {
        return origin.getEmail() + " -> " + destination.getEmail() + " (weight=" + weight + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Edge other)) return false;
        return origin.equals(other.origin) && destination.equals(other.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origin, destination);
    }
}
