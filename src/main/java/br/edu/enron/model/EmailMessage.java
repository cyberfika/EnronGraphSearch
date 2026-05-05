package br.edu.enron.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Contém os dados extraídos de um único arquivo de e-mail bruto.
 *
 * <p>Esta classe <em>não</em> representa todo o arquivo bruto — apenas os campos 
 * relevantes para a construção do grafo: o endereço do remetente e a lista de 
 * endereços de destinatários. Um {@code EmailMessage} pode produzir várias arestas 
 * no grafo, uma para cada destinatário distinto.</p>
 *
 * <p>Destinatários duplicados na mesma mensagem são removidos no momento da construção 
 * para que um único e-mail não infle artificialmente os pesos das arestas.</p>
 */
public final class EmailMessage {

    /** Endereço de e-mail normalizado do remetente. */
    private final String sender;

    /**
     * Lista ordenada e sem duplicatas de endereços de e-mail de destinatários normalizados.
     * Armazenada como uma visão imutável para proteger o estado interno.
     */
    private final List<String> recipients;

    /**
     * Constrói um {@code EmailMessage} com o remetente e os destinatários fornecidos.
     *
     * <p>Endereços de destinatários duplicados (após a normalização) são removidos silenciosamente. 
     * Entradas {@code null} ou vazias na lista de destinatários também são descartadas.</p>
     *
     * @param sender      o endereço do remetente bruto; não deve ser {@code null} ou vazio.
     * @param recipients  lista bruta de endereços de destinatários; não deve ser {@code null}.
     * @throws IllegalArgumentException se {@code sender} for {@code null} ou vazio, 
     *                                  ou se {@code recipients} for {@code null}.
     */
    public EmailMessage(String sender, List<String> recipients) {
        if (sender == null || sender.isBlank()) {
            throw new IllegalArgumentException("Sender must not be null or blank.");
        }
        if (recipients == null) {
            throw new IllegalArgumentException("Recipients list must not be null.");
        }

        this.sender = sender.trim().toLowerCase();

        // Remover duplicatas e filtrar entradas inválidas preservando a ordem de inserção
        Set<String> seen = new LinkedHashSet<>();
        for (String r : recipients) {
            if (r != null && !r.isBlank()) {
                seen.add(r.trim().toLowerCase());
            }
        }
        this.recipients = List.copyOf(new ArrayList<>(seen));
    }

    /**
     * Retorna o endereço de e-mail normalizado do remetente.
     *
     * @return o endereço do remetente em letras minúsculas, sem espaços nas extremidades.
     */
    public String getSender() {
        return sender;
    }

    /**
     * Retorna uma lista imutável e sem duplicatas de endereços de destinatários normalizados.
     *
     * @return lista de e-mails de destinatários; nunca {@code null}, pode estar vazia.
     */
    public List<String> getRecipients() {
        return recipients;
    }

    /**
     * Determina se esta mensagem carrega dados suficientes para produzir pelo menos uma 
     * aresta do grafo: um remetente não vazio e pelo menos um destinatário.
     *
     * @return {@code true} se a mensagem for utilizável para a construção do grafo.
     */
    public boolean hasValidData() {
        return !sender.isBlank() && !recipients.isEmpty();
    }

    /**
     * Retorna uma representação em string compacta para fins de log e depuração.
     *
     * @return resumo formatado do remetente e contagem de destinatários.
     */
    @Override
    public String toString() {
        return "EmailMessage{sender='" + sender + "', recipients=" + recipients.size() + "}";
    }
}
