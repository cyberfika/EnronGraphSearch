package br.edu.enron.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Representa uma aresta direcionada e ponderada entre dois indivíduos no grafo de contatos.
 *
 * <p>Uma aresta de {@code origin} para {@code destination} é criada na primeira vez que
 * {@code origin} envia uma mensagem para {@code destination}. Cada mensagem subsequente
 * entre o mesmo par incrementa o {@link #weight} em um via {@link #incrementWeight()}, 
 * portanto, o peso sempre reflete o número total de mensagens enviadas da origem para o destino.</p>
 *
 * <p>A aresta é direcional: uma aresta A→B é inteiramente independente de uma aresta B→A. 
 * Isso corresponde à semântica do dataset da Enron, onde o envio e o recebimento são papéis assimétricos.</p>
 *
 * <p>O método {@link #getInverseCost()} retorna {@code 1.0 / weight} e é usado pelo algoritmo 
 * de Dijkstra adaptado no {@code CriticalPathService}: uma aresta de peso alto (forte dependência de comunicação) 
 * é mapeada para um custo baixo, de modo que o caminho mais curto através dos custos inversos 
 * corresponde ao caminho de dependência máxima.</p>
 */
public final class Edge implements Serializable {

    private static final long serialVersionUID = 1L;

    /** O vértice remetente — fonte da aresta direcionada. */
    private final Vertex origin;

    /** O vértice destinatário — destino da aresta direcionada. */
    private final Vertex destination;

    /**
     * Número de mensagens enviadas de {@link #origin} para {@link #destination}.
     * Começa em 1 e cresce via {@link #incrementWeight()}.
     */
    private int weight;

    /**
     * Constrói uma nova aresta com um peso inicial de 1.
     *
     * @param origin      o vértice remetente; não deve ser {@code null}.
     * @param destination o vértice destinatário; não deve ser {@code null}.
     * @throws IllegalArgumentException se qualquer vértice for {@code null}.
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
     * Retorna o vértice de origem (remetente) desta aresta direcionada.
     *
     * @return o vértice remetente.
     */
    public Vertex getOrigin() {
        return origin;
    }

    /**
     * Retorna o vértice de destino (destinatário) desta aresta direcionada.
     *
     * @return o vértice destinatário.
     */
    public Vertex getDestination() {
        return destination;
    }

    /**
     * Retorna o peso atual desta aresta, ou seja, o número total de mensagens
     * enviadas de {@link #origin} para {@link #destination}.
     *
     * @return um número inteiro positivo representando a frequência das mensagens.
     */
    public int getWeight() {
        return weight;
    }

    /**
     * Incrementa o peso da aresta em um, registrando uma mensagem adicional enviada
     * de {@link #origin} para {@link #destination}.
     */
    public void incrementWeight() {
        this.weight++;
    }

    /**
     * Retorna o custo inverso usado pelo algoritmo de Dijkstra adaptado.
     *
     * <p>Fórmula: {@code 1.0 / weight}. Uma aresta mais pesada (mais mensagens) produz um
     * custo menor, portanto, o Dijkstra minimizando este valor favorecerá caminhos que passam
     * por links de comunicação de alta frequência — resultando no caminho crítico
     * (de dependência máxima).</p>
     *
     * @return {@code 1.0 / weight}, sempre um double positivo.
     */
    public double getInverseCost() {
        return 1.0 / weight;
    }

    /**
     * Retorna uma representação legível da aresta, incluindo origem,
     * destino e peso atual.
     *
     * @return string da aresta formatada.
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
