package br.edu.enron.parser;

import br.edu.enron.graph.ContactGraph;
import br.edu.enron.model.EmailMessage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Percorre a árvore de diretórios do Enron Email Dataset, lê arquivos de e-mail das 
 * subpastas {@code sent} e {@code _sent_mail} de cada usuário, remove duplicatas exatas 
 * que aparecem em ambas as pastas e constrói um {@link ContactGraph}.
 *
 * <h2>Seleção de pastas</h2>
 * <p>Apenas as subpastas {@code sent} e {@code _sent_mail} são utilizadas. Estas 
 * representam mensagens enviadas sob a perspectiva do proprietário da caixa de correio, 
 * que é exatamente o que precisamos: o remetente é o proprietário da pasta (ou o valor 
 * no cabeçalho {@code From:}), e os destinatários estão no campo {@code To:}.</p>
 *
 * <h2>Deduplicação</h2>
 * <p>Alguns usuários possuem tanto uma pasta {@code sent} quanto uma {@code _sent_mail} 
 * que contêm mensagens sobrepostas. Para evitar contar o mesmo e-mail duas vezes 
 * (o que inflaria artificialmente os pesos das arestas), o conteúdo de cada arquivo 
 * é submetido a um hash SHA-256. Se o mesmo hash aparecer em ambas as pastas para um 
 * determinado usuário, apenas a primeira ocorrência encontrada é processada.</p>
 *
 * <h2>Regra de cabeçalho de thread</h2>
 * <p>Apenas o bloco de cabeçalho de nível superior de cada arquivo é analisado 
 * (tudo antes da primeira linha em branco). Cabeçalhos citados ou encaminhados 
 * dentro do corpo são ignorados. Consulte {@link EmailParser} para detalhes.</p>
 *
 * <h2>Cache binário</h2>
 * <p>Após a análise, o {@link ContactGraph} resultante é serializado para um arquivo binário 
 * ({@code graph.bin}) ao lado da raiz do dataset. Em execuções subsequentes, o cache 
 * é carregado diretamente, ignorando a varredura completa dos arquivos. Passe {@code --rebuild} 
 * como um argumento CLI (tratado em {@code Main}) para forçar a reanálise mesmo quando o 
 * cache existir.</p>
 */
public class EnronDatasetReader {

    private static final Logger LOG = Logger.getLogger(EnronDatasetReader.class.getName());

    /** Nome do arquivo de cache binário gravado ao lado do diretório raiz do dataset. */
    public static final String CACHE_FILENAME = "graph.bin";

    /** Nomes das subpastas a serem lidas de cada diretório de usuário. */
    private static final Set<String> TARGET_FOLDERS = Set.of("sent", "_sent_mail");

    private final EmailParser parser;

    // Contadores para relatórios
    private int filesRead;
    private int filesSkipped;
    private int duplicatesDropped;
    private int messagesValid;

    /**
     * Constrói um leitor com um {@link EmailParser} padrão.
     */
    public EnronDatasetReader() {
        this.parser = new EmailParser();
    }

    // -------------------------------------------------------------------------
    // API de Cache
    // -------------------------------------------------------------------------

    /**
     * Retorna o caminho onde o cache binário do grafo será gravado, colocado 
     * ao lado do diretório raiz do dataset.
     *
     * @param datasetRoot o diretório base do dataset Enron.
     * @return o caminho do arquivo de cache.
     */
    public static Path cachePath(Path datasetRoot) {
        return datasetRoot.getParent().resolve(CACHE_FILENAME);
    }

    /**
     * Tenta carregar um {@link ContactGraph} previamente serializado a partir do 
     * arquivo de cache binário.
     *
     * @param cacheFile caminho para o arquivo de cache {@code .bin}.
     * @return o grafo desserializado, ou {@code null} se o carregamento falhar.
     */
    public ContactGraph loadFromCache(Path cacheFile) {
        if (!Files.exists(cacheFile)) return null;
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(cacheFile)))) {
            Object obj = ois.readObject();
            if (obj instanceof ContactGraph g) {
                LOG.info("Grafo carregado do cache: " + cacheFile);
                return g;
            }
        } catch (IOException | ClassNotFoundException e) {
            LOG.warning("Falha no carregamento do cache (" + e.getMessage() + "); será reconstruído.");
        }
        return null;
    }

    /**
     * Serializa o grafo fornecido para o arquivo de cache binário para uso futuro.
     *
     * @param graph     o grafo a ser persistido.
     * @param cacheFile caminho de destino para o arquivo de cache.
     */
    public void saveToCache(ContactGraph graph, Path cacheFile) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(Files.newOutputStream(cacheFile)))) {
            oos.writeObject(graph);
            LOG.info("Grafo salvo no cache: " + cacheFile);
        } catch (IOException e) {
            LOG.warning("Não foi possível salvar o cache do grafo: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Construção do Grafo
    // -------------------------------------------------------------------------

    /**
     * Constrói um {@link ContactGraph} varrendo todos os diretórios de usuários sob 
     * {@code datasetRoot}, lendo apenas as subpastas {@code sent} e {@code _sent_mail}, 
     * removendo duplicatas de conteúdo de arquivo entre as duas pastas por usuário 
     * e analisando cada arquivo de e-mail único.
     *
     * <p>Arquivos que não podem ser lidos são contados como pulados e não interrompem 
     * a varredura geral.</p>
     *
     * @param datasetRoot o diretório raiz contendo um subdiretório por usuário 
     *                    (ex: {@code data/maildir}).
     * @return o grafo de contatos preenchido.
     * @throws IllegalArgumentException se {@code datasetRoot} não for um diretório existente.
     */
    public ContactGraph buildGraph(Path datasetRoot) {
        if (!Files.isDirectory(datasetRoot)) {
            throw new IllegalArgumentException("A raiz do dataset não é um diretório: " + datasetRoot);
        }

        resetCounters();
        ContactGraph graph = new ContactGraph();

        // Cada subdiretório imediato é a caixa de correio de um usuário
        File[] userDirs = datasetRoot.toFile().listFiles(File::isDirectory);
        if (userDirs == null) return graph;

        Arrays.sort(userDirs); // ordenação determinística para reprodutibilidade

        for (File userDir : userDirs) {
            processUserDirectory(userDir.toPath(), graph);
        }

        printReport();
        return graph;
    }

    // -------------------------------------------------------------------------
    // Auxiliares privados
    // -------------------------------------------------------------------------

    /**
     * Processa a caixa de correio de um usuário: coleta arquivos de {@code sent} e 
     * {@code _sent_mail}, remove duplicatas e, em seguida, analisa e insere cada 
     * mensagem única no grafo.
     *
     * @param userDir o diretório da caixa de correio do usuário.
     * @param graph   o grafo que está sendo construído.
     */
    private void processUserDirectory(Path userDir, ContactGraph graph) {
        // Coletar todos os arquivos candidatos por pasta de destino
        Map<String, List<Path>> folderFiles = new LinkedHashMap<>();
        for (String folderName : TARGET_FOLDERS) {
            Path subDir = userDir.resolve(folderName);
            if (Files.isDirectory(subDir)) {
                folderFiles.put(folderName, listEmailFiles(subDir));
            }
        }

        // Remover duplicatas entre pastas usando hashes de conteúdo
        Set<String> seenHashes = new HashSet<>();
        List<Path> uniqueFiles  = new ArrayList<>();

        for (List<Path> files : folderFiles.values()) {
            for (Path file : files) {
                String hash = contentHash(file);
                if (hash == null) {
                    filesSkipped++;
                    continue;
                }
                if (seenHashes.add(hash)) {
                    uniqueFiles.add(file);
                } else {
                    duplicatesDropped++;
                }
            }
        }

        // Analisar e ingerir cada mensagem única
        for (Path file : uniqueFiles) {
            filesRead++;
            String raw = readFile(file);
            if (raw == null) {
                filesSkipped++;
                continue;
            }
            EmailMessage msg = parser.parse(raw);
            if (!msg.hasValidData()) {
                filesSkipped++;
                continue;
            }
            // Registrar este remetente como proprietário de caixa de correio (filtra o combo box De)
            graph.addOwner(msg.getSender());
            for (String recipient : msg.getRecipients()) {
                graph.addEdge(msg.getSender(), recipient);
            }
            messagesValid++;
        }
    }

    /**
     * Lista todos os arquivos regulares em um diretório (não recursivo — as pastas sent 
     * da Enron são planas: cada arquivo é um e-mail).
     *
     * @param dir o diretório para listar.
     * @return lista ordenada de caminhos de arquivos.
     */
    private List<Path> listEmailFiles(Path dir) {
        File[] files = dir.toFile().listFiles(File::isFile);
        if (files == null) return List.of();
        Arrays.sort(files);
        List<Path> paths = new ArrayList<>(files.length);
        for (File f : files) paths.add(f.toPath());
        return paths;
    }

    /**
     * Lê todo o conteúdo de um arquivo como uma string UTF-8.
     *
     * @param file caminho para o arquivo.
     * @return conteúdo do arquivo, ou {@code null} se ocorrer um erro de E/S.
     */
    private String readFile(Path file) {
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Calcula um hash SHA-256 codificado em hexadecimal do conteúdo do arquivo para deduplicação.
     *
     * @param file caminho para o arquivo.
     * @return string de hash hexadecimal, ou {@code null} em caso de erro.
     */
    private String contentHash(Path file) {
        try {
            byte[] bytes = Files.readAllBytes(file);
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(bytes);
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /** Redefine todos os contadores antes de uma nova varredura. */
    private void resetCounters() {
        filesRead = filesSkipped = duplicatesDropped = messagesValid = 0;
    }

    /** Imprime um resumo da varredura na saída padrão. */
    private void printReport() {
        System.out.println("=== Varredura do dataset concluída ===");
        System.out.println("  Arquivos lidos            : " + filesRead);
        System.out.println("  Duplicatas descartadas    : " + duplicatesDropped);
        System.out.println("  Arquivos pulados          : " + filesSkipped);
        System.out.println("  Mensagens válidas         : " + messagesValid);
    }
}
