package br.edu.enron.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Representa um indivíduo na rede de contatos da Enron.
 *
 * <p>Cada vértice corresponde a um endereço de e-mail exclusivo. Ele serve como o nó rotulado
 * do grafo direcionado e ponderado: o rótulo é o próprio e-mail, normalizado para letras minúsculas 
 * e sem espaços em branco nas extremidades, para que dois endereços que diferem apenas em maiúsculas 
 * ou espaços sejam tratados como a mesma pessoa.</p>
 *
 * <p>{@link #equals(Object)} e {@link #hashCode()} baseiam-se exclusivamente na string do e-mail, 
 * o que permite que os vértices sejam armazenados com segurança em coleções {@code HashMap} e
 * {@code HashSet} e pesquisados por endereço sem a necessidade de manter uma referência direta ao objeto.</p>
 */
public final class Vertex implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Endereço de e-mail normalizado que identifica exclusivamente este vértice. */
    private final String email;

    /**
     * Constrói um vértice para o endereço de e-mail fornecido.
     *
     * @param email o endereço de e-mail bruto; não deve ser {@code null} ou vazio.
     * @throws IllegalArgumentException se {@code email} for {@code null} ou vazio.
     */
    public Vertex(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Vertex email must not be null or blank.");
        }
        this.email = email.trim().toLowerCase();
    }

    /**
     * Retorna o endereço de e-mail normalizado que rotula este vértice.
     *
     * @return a string do e-mail em minúsculas e sem espaços nas extremidades.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Dois vértices são iguais se, e somente se, seus endereços de e-mail normalizados forem iguais.
     *
     * @param obj o objeto a ser comparado.
     * @return {@code true} se {@code obj} for um {@code Vertex} com o mesmo e-mail.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Vertex other)) return false;
        return email.equals(other.email);
    }

    /**
     * Código hash derivado exclusivamente do e-mail normalizado para que o 
     * contrato {@code equals → mesmo hashCode} seja satisfeito.
     *
     * @return código hash da string do e-mail.
     */
    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    /**
     * Retorna o endereço de e-mail como a representação em string deste vértice.
     *
     * @return o e-mail normalizado.
     */
    @Override
    public String toString() {
        return email;
    }
}
