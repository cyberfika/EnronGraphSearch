package br.edu.enron.parser;

import br.edu.enron.model.EmailMessage;
import br.edu.enron.util.EmailValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Analisa o conteúdo de texto bruto de um único arquivo de e-mail da Enron e extrai 
 * os endereços de remetente e destinatário necessários para a construção do grafo.
 *
 * <h2>Estratégia de análise (parsing)</h2>
 * <p>Os arquivos de e-mail da Enron seguem o formato RFC 2822: um bloco de cabeçalho 
 * no topo, separado do corpo da mensagem pela primeira linha em branco. Este analisador 
 * lê <em>apenas</em> o bloco de cabeçalho inicial e para na primeira linha em branco. 
 * Quaisquer cabeçalhos citados ou encaminhados incorporados dentro do corpo (ex: linhas 
 * que começam com {@code >}, ou blocos introduzidos por {@code -----Original Message-----}) 
 * são completamente ignorados, pois pertencem a mensagens anteriores na conversa 
 * e distorceriam os pesos das arestas se processados.</p>
 *
 * <h2>Campos extraídos</h2>
 * <ul>
 *   <li>{@code From:} — endereço do remetente (um único valor).</li>
 *   <li>{@code To:} — endereços de destinatários separados por vírgula.</li>
 * </ul>
 * <p>{@code Cc:} e {@code Bcc:} são deliberadamente excluídos: as pastas {@code sent} / 
 * {@code _sent_mail} do dataset refletem a perspectiva do remetente, e o campo {@code To:} 
 * já captura os destinatários diretos. Incluir Cc/Bcc exigiria uma decisão de política 
 * sobre se uma cópia constitui o mesmo tipo de dependência de comunicação, o que está 
 * fora do escopo do projeto.</p>
 *
 * <h2>Limitações</h2>
 * <ul>
 *   <li>O dobramento de cabeçalho em várias linhas (uma linha de continuação que começa 
 *       com espaço em branco) é tratado para {@code To:} anexando as linhas dobradas 
 *       ao campo atual.</li>
 *   <li>Endereços que não contêm {@code @} são descartados por 
 *       {@link EmailValidator#isValid(String)}.</li>
 * </ul>
 */
public class EmailParser {

    /**
     * Analisa o conteúdo bruto de um arquivo de e-mail e retorna um {@link EmailMessage}
     * contendo a lista normalizada de remetente e destinatários extraída apenas do 
     * bloco de cabeçalho de nível superior.
     *
     * <p>Se o conteúdo for {@code null}, vazio ou carecer de um cabeçalho {@code From:} 
     * válido, o {@code EmailMessage} retornado terá uma lista de destinatários vazia 
     * e {@link EmailMessage#hasValidData()} retornará {@code false}.</p>
     *
     * @param rawContent o texto completo do arquivo de e-mail; pode ser {@code null}.
     * @return {@link EmailMessage} analisado; nunca {@code null}.
     */
    public EmailMessage parse(String rawContent) {
        if (rawContent == null || rawContent.isBlank()) {
            return new EmailMessage("unknown@unknown.com", List.of());
        }

        String sender = null;
        List<String> recipients = new ArrayList<>();

        String[] lines = rawContent.split("\n", -1);

        // Rastrear qual cabeçalho de várias linhas estamos acumulando no momento
        String currentField = null;
        StringBuilder currentValue = new StringBuilder();

        for (String rawLine : lines) {
            // A primeira linha em branco marca o fim do bloco de cabeçalho — pare aqui.
            if (rawLine.isBlank()) break;

            boolean isFolded = rawLine.length() > 0
                    && (rawLine.charAt(0) == ' ' || rawLine.charAt(0) == '\t');

            if (isFolded && currentField != null) {
                // Continuação do campo de cabeçalho anterior
                currentValue.append(' ').append(rawLine.trim());
                continue;
            }

            // Antes de iniciar um novo campo, processe o que foi acumulado anteriormente
            if (currentField != null) {
                processField(currentField, currentValue.toString(), recipients);
                if ("from".equals(currentField) && sender == null) {
                    sender = extractSingleAddress(currentValue.toString());
                }
                currentField = null;
                currentValue.setLength(0);
            }

            // Identificar o novo campo
            int colon = rawLine.indexOf(':');
            if (colon <= 0) continue;

            String fieldName = rawLine.substring(0, colon).trim().toLowerCase();
            String fieldValue = rawLine.substring(colon + 1).trim();

            if ("from".equals(fieldName) || "to".equals(fieldName)) {
                currentField = fieldName;
                currentValue.append(fieldValue);
            }
            // Todos os outros cabeçalhos (Subject, Date, Message-ID, Cc, Bcc, X-*, …) são ignorados
        }

        // Processar o último campo acumulado
        if (currentField != null) {
            if ("from".equals(currentField) && sender == null) {
                sender = extractSingleAddress(currentValue.toString());
            }
            processField(currentField, currentValue.toString(), recipients);
        }

        if (sender == null || !EmailValidator.isValid(sender)) {
            return new EmailMessage("unknown@unknown.com", List.of());
        }

        return new EmailMessage(sender, recipients);
    }

    // -------------------------------------------------------------------------
    // Auxiliares privados
    // -------------------------------------------------------------------------

    /**
     * Anexa endereços de destinatários válidos analisados a partir de um valor de campo 
     * {@code To:} à lista em execução. Não faz nada para campos que não sejam {@code to}.
     *
     * @param field      o nome do campo de cabeçalho em minúsculas.
     * @param value      o valor do campo acumulado (pode abranger várias linhas dobradas).
     * @param recipients a lista na qual os endereços válidos serão anexados.
     */
    private void processField(String field, String value, List<String> recipients) {
        if (!"to".equals(field)) return;
        // Os destinatários são separados por vírgula; divida e valide cada um
        for (String part : value.split(",")) {
            String addr = EmailValidator.normalize(extractSingleAddress(part));
            if (EmailValidator.isValid(addr)) {
                recipients.add(addr);
            }
        }
    }

    /**
     * Extrai um endereço de e-mail puro de uma string que pode estar qualificada 
     * com um nome de exibição, como {@code "John Doe <john@example.com>"} ou 
     * simplesmente {@code "john@example.com"}.
     *
     * @param raw o token de endereço bruto.
     * @return o endereço puro, ou a entrada limpa se nenhum colchete angular for encontrado.
     */
    private String extractSingleAddress(String raw) {
        if (raw == null) return "";
        int lt = raw.indexOf('<');
        int gt = raw.indexOf('>');
        if (lt >= 0 && gt > lt) {
            return raw.substring(lt + 1, gt).trim();
        }
        return raw.trim();
    }
}
