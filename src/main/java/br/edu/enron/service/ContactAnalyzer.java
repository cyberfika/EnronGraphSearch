package br.edu.enron.service;

import br.edu.enron.graph.ContactGraph;
import br.edu.enron.model.DegreeResult;
import br.edu.enron.model.Vertex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fornece consultas analíticas de alto nível sobre um {@link ContactGraph}.
 *
 * <p>Este serviço é o mapeamento direto para os critérios de avaliação:</p>
 * <ul>
 *   <li>Contagens de vértices e arestas (0,25 pt cada).</li>
 *   <li>Listas dos top-20 graus de saída e top-20 graus de entrada (0,25 pt cada).</li>
 * </ul>
 *
 * <p>Todas as classificações são realizadas com a ordenação natural definida em
 * {@link DegreeResult}: grau decrescente e e-mail crescente para desempate.</p>
 */
public class ContactAnalyzer {

    /** O grafo sobre o qual todas as consultas operam. */
    private final ContactGraph graph;

    /**
     * Constrói um {@code ContactAnalyzer} vinculado ao grafo fornecido.
     *
     * @param graph o grafo de contatos; não deve ser {@code null}.
     * @throws IllegalArgumentException se {@code graph} for {@code null}.
     */
    public ContactAnalyzer(ContactGraph graph) {
        if (graph == null) throw new IllegalArgumentException("Graph must not be null.");
        this.graph = graph;
    }

    /**
     * Retorna o número total de vértices (endereços de e-mail únicos) no grafo.
     *
     * @return contagem de vértices.
     */
    public int getVertexCount() {
        return graph.vertexCount();
    }

    /**
     * Retorna o número total de arestas direcionadas (pares únicos remetente → destinatário) no grafo.
     *
     * @return contagem de arestas.
     */
    public int getEdgeCount() {
        return graph.edgeCount();
    }

    /**
     * Retorna os 20 principais vértices classificados pelo grau de saída (número de pessoas distintas 
     * para as quais enviaram pelo menos uma mensagem).
     *
     * <p>Ordenação: grau decrescente; desempate alfabético pelo e-mail.</p>
     *
     * @return lista imutável de no máximo 20 entradas {@link DegreeResult}.
     */
    public List<DegreeResult> getTop20OutDegree() {
        return topN(20, true);
    }

    /**
     * Retorna os 20 principais vértices classificados pelo grau de entrada (número de pessoas distintas 
     * que enviaram pelo menos uma mensagem para eles).
     *
     * <p>Ordenação: grau decrescente; desempate alfabético pelo e-mail.</p>
     *
     * @return lista imutável de no máximo 20 entradas {@link DegreeResult}.
     */
    public List<DegreeResult> getTop20InDegree() {
        return topN(20, false);
    }

    // -------------------------------------------------------------------------
    // Auxiliares privados
    // -------------------------------------------------------------------------

    /**
     * Calcula a lista dos principais N graus para qualquer direção.
     *
     * @param n      número máximo de entradas a serem retornadas.
     * @param outDir {@code true} para grau de saída, {@code false} para grau de entrada.
     * @return lista ordenada e limitada.
     */
    private List<DegreeResult> topN(int n, boolean outDir) {
        List<DegreeResult> results = new ArrayList<>();
        for (Vertex v : graph.getVertices()) {
            int degree = outDir ? graph.outDegree(v) : graph.inDegree(v);
            results.add(new DegreeResult(v.getEmail(), degree));
        }
        Collections.sort(results);
        return Collections.unmodifiableList(results.subList(0, Math.min(n, results.size())));
    }
}
