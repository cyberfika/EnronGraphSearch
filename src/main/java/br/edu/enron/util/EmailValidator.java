package br.edu.enron.util;

/**
 * Classe utilitária para normalização e validação de endereços de e-mail.
 *
 * <p>A validação aqui é intencionalmente mínima: o dataset da Enron contém muitos
 * endereços não padronizados (aliases internos, listas de distribuição, etc.), 
 * portanto, uma validação estrita do RFC 5321 rejeitaria entradas legítimas. 
 * O único requisito rígido é a presença do {@code @} e uma string não vazia em cada lado.</p>
 */
public final class EmailValidator {

    private EmailValidator() {
        // classe utilitária — sem instâncias
    }

    /**
     * Normaliza um endereço de e-mail removendo espaços em branco nas extremidades 
     * e convertendo para minúsculas.
     *
     * @param email o endereço bruto; pode ser {@code null}.
     * @return endereço normalizado, ou uma string vazia se {@code email} for {@code null}.
     */
    public static String normalize(String email) {
        if (email == null) return "";
        return email.trim().toLowerCase();
    }

    /**
     * Retorna {@code true} se a string fornecida for um endereço de e-mail utilizável para fins 
     * de construção do grafo: não nulo, não vazio e contendo exatamente um caractere {@code @} 
     * com partes local e de domínio não vazias.
     *
     * @param email o endereço a ser validado (bruto ou já normalizado).
     * @return {@code true} se o endereço puder ser usado como rótulo de vértice.
     */
    public static boolean isValid(String email) {
        if (email == null || email.isBlank()) return false;
        String trimmed = email.trim();
        int atIndex = trimmed.indexOf('@');
        if (atIndex <= 0) return false;                      // sem @ ou nada antes dele
        if (atIndex == trimmed.length() - 1) return false;  // nada depois do @
        return trimmed.indexOf('@', atIndex + 1) == -1;     // apenas um @
    }
}
