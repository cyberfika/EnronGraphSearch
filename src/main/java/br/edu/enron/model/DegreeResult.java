package br.edu.enron.model;

/**
 * Registro imutável do resultado de uma consulta de grau para um único vértice.
 *
 * <p>Usado pelo {@code ContactAnalyzer} para retornar as listas dos top-20 para grau 
 * de entrada e grau de saída. A imutabilidade garante que os objetos de resultado 
 * passados ou armazenados em coleções permaneçam consistentes.</p>
 */
public final class DegreeResult implements Comparable<DegreeResult> {

    /** Endereço de e-mail normalizado do vértice. */
    private final String email;

    /**
     * Valor do grau — número de vizinhos de entrada ou vizinhos de saída distintos, 
     * dependendo da consulta que produziu este resultado.
     */
    private final int degree;

    /**
     * Constrói um {@code DegreeResult}.
     *
     * @param email  o e-mail do vértice; não deve ser {@code null} ou vazio.
     * @param degree o valor do grau; deve ser não negativo.
     * @throws IllegalArgumentException se o {@code email} for inválido ou o {@code degree} for negativo.
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
     * Retorna o endereço de e-mail do vértice.
     *
     * @return a string do e-mail.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Retorna o grau do vértice para a direção consultada.
     *
     * @return valor do grau não negativo.
     */
    public int getDegree() {
        return degree;
    }

    /**
     * Ordenação natural: decrescente pelo grau e, em seguida, crescente pelo e-mail para desempate. 
     * Esta ordenação é usada diretamente ao classificar as listas dos top-20.
     *
     * @param other o resultado a ser comparado.
     * @return negativo se este deve aparecer antes de {@code other}.
     */
    @Override
    public int compareTo(DegreeResult other) {
        int cmp = Integer.compare(other.degree, this.degree); // grau decrescente
        if (cmp != 0) return cmp;
        return this.email.compareTo(other.email);             // e-mail crescente
    }

    /**
     * Retorna uma linha formatada adequada para exibição no console.
     *
     * @return string no formato {@code email (degree=N)}.
     */
    @Override
    public String toString() {
        return email + " (degree=" + degree + ")";
    }
}
