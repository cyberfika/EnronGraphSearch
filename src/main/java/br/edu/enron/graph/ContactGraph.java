package br.edu.enron.graph;

import br.edu.enron.model.Edge;
import br.edu.enron.model.Vertex;

import java.io.Serializable;
import java.util.*;

/**
 * Directed, weighted, labeled contact graph built from the Enron Email Dataset.
 *
 * <h2>Internal representation</h2>
 * <p>The graph uses an adjacency map of maps:
 * <pre>
 *   adjacency : Map&lt;Vertex, Map&lt;Vertex, Edge&gt;&gt;
 * </pre>
 * The outer map's key is the <em>origin</em> vertex. The inner map's key is the
 * <em>destination</em> vertex, and its value is the {@link Edge} between them.
 * This layout gives O(1) average-case lookup for both "does edge A→B exist?" and
 * "what are all neighbours of A?", which is essential for large graphs.</p>
 *
 * <h2>Semantics</h2>
 * <ul>
 *   <li>Each vertex is a unique email address (the graph label).</li>
 *   <li>Each edge A→B records the number of messages sent from A to B (the weight).
 *       Adding the same edge again increments its weight instead of duplicating it.</li>
 *   <li>Degree (in or out) is counted as the number of <em>distinct</em> neighbours,
 *       not the sum of weights.</li>
 * </ul>
 *
 * <p>This class implements {@link Serializable} so the built graph can be persisted
 * to a binary cache file and reloaded on subsequent runs without re-parsing the
 * entire dataset.</p>
 */
public class ContactGraph implements Serializable {

    @java.io.Serial
    private static final long serialVersionUID = 2L;

    /**
     * Index of all vertices keyed by their normalized email address.
     * Provides O(1) vertex lookup by email string.
     */
    private final Map<String, Vertex> vertexIndex;

    /**
     * Adjacency structure: origin → (destination → edge).
     * Every vertex present in the graph has an entry here, even if it has no
     * outgoing edges (its inner map will simply be empty).
     */
    private final Map<Vertex, Map<Vertex, Edge>> adjacency;

    /**
     * Email addresses of the 150 Enron mailbox owners — i.e., the senders found
     * in {@code sent} / {@code _sent_mail} folders. Used to populate the "From"
     * combo box with only real dataset users, not external addresses.
     */
    private final Set<String> ownerEmails;

    /**
     * Constructs an empty contact graph.
     */
    public ContactGraph() {
        this.vertexIndex  = new HashMap<>();
        this.adjacency    = new HashMap<>();
        this.ownerEmails  = new TreeSet<>(); // sorted for predictable combo order
    }

    /**
     * Registers an email address as a mailbox owner (sender from a sent folder).
     * Only these addresses appear in the "From" combo box of the search panel.
     *
     * @param email the owner's normalized email address.
     */
    public void addOwner(String email) {
        if (email != null && !email.isBlank()) {
            ownerEmails.add(email.trim().toLowerCase());
        }
    }

    /**
     * Returns an unmodifiable, alphabetically sorted set of mailbox-owner emails.
     * In dataset mode this corresponds to the 150 Enron users whose sent folders
     * were processed. In demo mode it contains the hand-crafted senders.
     *
     * @return sorted set of owner email addresses; never {@code null}.
     */
    public Set<String> getOwnerEmails() {
        return Collections.unmodifiableSet(ownerEmails);
    }

    // -------------------------------------------------------------------------
    // Mutation
    // -------------------------------------------------------------------------

    /**
     * Ensures a vertex with the given email exists in the graph, creating it if
     * necessary, and returns it.
     *
     * @param email the email address; must not be {@code null} or blank.
     * @return the existing or newly created {@link Vertex}.
     * @throws IllegalArgumentException if {@code email} is {@code null} or blank.
     */
    public Vertex addVertex(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be null or blank.");
        }
        String key = email.trim().toLowerCase();
        return vertexIndex.computeIfAbsent(key, k -> {
            Vertex v = new Vertex(k);
            adjacency.put(v, new HashMap<>());
            return v;
        });
    }

    /**
     * Adds a directed edge from {@code originEmail} to {@code destinationEmail}.
     *
     * <p>If the edge already exists its weight is incremented by one. If either
     * vertex does not yet exist it is created automatically. Self-loops
     * (origin == destination) are silently ignored as they carry no meaningful
     * information for the contact network.</p>
     *
     * @param originEmail      the sender's email address.
     * @param destinationEmail the recipient's email address.
     * @throws IllegalArgumentException if either address is {@code null} or blank.
     */
    public void addEdge(String originEmail, String destinationEmail) {
        if (originEmail == null || originEmail.isBlank()) {
            throw new IllegalArgumentException("Origin email must not be null or blank.");
        }
        if (destinationEmail == null || destinationEmail.isBlank()) {
            throw new IllegalArgumentException("Destination email must not be null or blank.");
        }

        String normOrigin = originEmail.trim().toLowerCase();
        String normDest   = destinationEmail.trim().toLowerCase();

        // Ignore self-loops
        if (normOrigin.equals(normDest)) return;

        Vertex origin      = addVertex(normOrigin);
        Vertex destination = addVertex(normDest);

        Map<Vertex, Edge> outEdges = adjacency.get(origin);
        Edge existing = outEdges.get(destination);
        if (existing != null) {
            existing.incrementWeight();
        } else {
            outEdges.put(destination, new Edge(origin, destination));
        }
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    /**
     * Looks up a vertex by its email address.
     *
     * @param email the normalized or raw email to search for.
     * @return an {@link Optional} containing the vertex, or empty if not found.
     */
    public Optional<Vertex> findVertex(String email) {
        if (email == null || email.isBlank()) return Optional.empty();
        return Optional.ofNullable(vertexIndex.get(email.trim().toLowerCase()));
    }

    /**
     * Returns {@code true} if a vertex with the given email exists in the graph.
     *
     * @param email the email to check.
     * @return {@code true} if found.
     */
    public boolean containsVertex(String email) {
        if (email == null || email.isBlank()) return false;
        return vertexIndex.containsKey(email.trim().toLowerCase());
    }

    /**
     * Returns an unmodifiable view of all vertices in the graph.
     *
     * @return collection of all vertices; never {@code null}.
     */
    public Collection<Vertex> getVertices() {
        return Collections.unmodifiableCollection(vertexIndex.values());
    }

    /**
     * Returns an unmodifiable list of all edges in the graph.
     *
     * <p>This is O(V + E) as it flattens the adjacency map.</p>
     *
     * @return list of all directed edges; never {@code null}.
     */
    public List<Edge> getEdges() {
        List<Edge> all = new ArrayList<>();
        for (Map<Vertex, Edge> outMap : adjacency.values()) {
            all.addAll(outMap.values());
        }
        return Collections.unmodifiableList(all);
    }

    /**
     * Returns the outgoing edges from the given vertex.
     *
     * @param vertex the origin vertex; must not be {@code null}.
     * @return unmodifiable list of outgoing edges; empty if the vertex has none.
     * @throws IllegalArgumentException if {@code vertex} is {@code null}.
     */
    public List<Edge> getOutEdges(Vertex vertex) {
        if (vertex == null) throw new IllegalArgumentException("Vertex must not be null.");
        Map<Vertex, Edge> outMap = adjacency.get(vertex);
        if (outMap == null) return Collections.emptyList();
        return Collections.unmodifiableList(new ArrayList<>(outMap.values()));
    }

    /**
     * Returns the incoming edges to the given vertex.
     *
     * <p>Because the adjacency map is indexed by origin, computing in-edges requires
     * a full scan of all vertices — O(V + E). This is acceptable for the top-20
     * queries which run once at startup.</p>
     *
     * @param vertex the destination vertex; must not be {@code null}.
     * @return unmodifiable list of incoming edges; empty if none.
     * @throws IllegalArgumentException if {@code vertex} is {@code null}.
     */
    public List<Edge> getInEdges(Vertex vertex) {
        if (vertex == null) throw new IllegalArgumentException("Vertex must not be null.");
        List<Edge> result = new ArrayList<>();
        for (Map<Vertex, Edge> outMap : adjacency.values()) {
            Edge e = outMap.get(vertex);
            if (e != null) result.add(e);
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns the total number of vertices in the graph.
     *
     * @return vertex count.
     */
    public int vertexCount() {
        return vertexIndex.size();
    }

    /**
     * Returns the total number of directed edges in the graph.
     *
     * <p>Note: this counts distinct edges, not the sum of their weights.</p>
     *
     * @return edge count.
     */
    public int edgeCount() {
        int count = 0;
        for (Map<Vertex, Edge> outMap : adjacency.values()) {
            count += outMap.size();
        }
        return count;
    }

    /**
     * Returns the out-degree of a vertex: the number of distinct recipients it
     * has ever sent at least one message to.
     *
     * @param vertex the vertex to query; must not be {@code null}.
     * @return out-degree, or 0 if the vertex has no outgoing edges.
     * @throws IllegalArgumentException if {@code vertex} is {@code null}.
     */
    public int outDegree(Vertex vertex) {
        if (vertex == null) throw new IllegalArgumentException("Vertex must not be null.");
        Map<Vertex, Edge> outMap = adjacency.get(vertex);
        return outMap == null ? 0 : outMap.size();
    }

    /**
     * Returns the in-degree of a vertex: the number of distinct senders who have
     * sent at least one message to this vertex.
     *
     * @param vertex the vertex to query; must not be {@code null}.
     * @return in-degree, or 0 if no one has sent to this vertex.
     * @throws IllegalArgumentException if {@code vertex} is {@code null}.
     */
    public int inDegree(Vertex vertex) {
        if (vertex == null) throw new IllegalArgumentException("Vertex must not be null.");
        int count = 0;
        for (Map<Vertex, Edge> outMap : adjacency.values()) {
            if (outMap.containsKey(vertex)) count++;
        }
        return count;
    }
}
