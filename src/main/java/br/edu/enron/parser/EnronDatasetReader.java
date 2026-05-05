package br.edu.enron.parser;

import br.edu.enron.graph.ContactGraph;
import br.edu.enron.model.EmailMessage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Walks the Enron Email Dataset directory tree, reads email files from
 * {@code sent} and {@code _sent_mail} subfolders of each user, deduplicates
 * exact copies that appear in both folders, and builds a {@link ContactGraph}.
 *
 * <h2>Folder selection</h2>
 * <p>Only the {@code sent} and {@code _sent_mail} subfolders are used. These
 * represent outgoing messages from the mailbox owner's perspective, which is
 * exactly what we need: the sender is the folder owner (or the value in the
 * {@code From:} header), and the recipients are in the {@code To:} field.</p>
 *
 * <h2>Deduplication</h2>
 * <p>Some users have both a {@code sent} and a {@code _sent_mail} folder that
 * contain overlapping messages. To avoid counting the same email twice
 * (which would artificially inflate edge weights), each file's content is
 * hashed with SHA-256. If the same hash appears in both folders for a given
 * user, only the first occurrence encountered is processed.</p>
 *
 * <h2>Thread header rule</h2>
 * <p>Only the top-level header block of each file is parsed (everything before
 * the first blank line). Quoted or forwarded headers inside the body are ignored.
 * See {@link EmailParser} for details.</p>
 *
 * <h2>Binary cache</h2>
 * <p>After parsing, the resulting {@link ContactGraph} is serialized to a binary
 * file ({@code graph.bin}) next to the dataset root. On subsequent runs the cache
 * is loaded directly, bypassing the full file scan. Pass {@code --rebuild} as a
 * CLI argument (handled in {@code Main}) to force re-parsing even when the cache
 * exists.</p>
 */
public class EnronDatasetReader {

    private static final Logger LOG = Logger.getLogger(EnronDatasetReader.class.getName());

    /** Name of the binary cache file written next to the dataset root directory. */
    public static final String CACHE_FILENAME = "graph.bin";

    /** Subfolder names to read from each user directory. */
    private static final Set<String> TARGET_FOLDERS = Set.of("sent", "_sent_mail");

    private final EmailParser parser;

    // Counters for reporting
    private int filesRead;
    private int filesSkipped;
    private int duplicatesDropped;
    private int messagesValid;

    /**
     * Constructs a reader with a default {@link EmailParser}.
     */
    public EnronDatasetReader() {
        this.parser = new EmailParser();
    }

    // -------------------------------------------------------------------------
    // Cache API
    // -------------------------------------------------------------------------

    /**
     * Returns the path where the binary graph cache will be written, placed
     * alongside the dataset root directory.
     *
     * @param datasetRoot the base directory of the Enron dataset.
     * @return the cache file path.
     */
    public static Path cachePath(Path datasetRoot) {
        return datasetRoot.getParent().resolve(CACHE_FILENAME);
    }

    /**
     * Attempts to load a previously serialized {@link ContactGraph} from the
     * binary cache file.
     *
     * @param cacheFile path to the {@code .bin} cache file.
     * @return the deserialized graph, or {@code null} if loading fails.
     */
    public ContactGraph loadFromCache(Path cacheFile) {
        if (!Files.exists(cacheFile)) return null;
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(cacheFile)))) {
            Object obj = ois.readObject();
            if (obj instanceof ContactGraph g) {
                LOG.info("Graph loaded from cache: " + cacheFile);
                return g;
            }
        } catch (IOException | ClassNotFoundException e) {
            LOG.warning("Cache load failed (" + e.getMessage() + "); will rebuild.");
        }
        return null;
    }

    /**
     * Serializes the given graph to the binary cache file for future reuse.
     *
     * @param graph     the graph to persist.
     * @param cacheFile destination path for the cache file.
     */
    public void saveToCache(ContactGraph graph, Path cacheFile) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(Files.newOutputStream(cacheFile)))) {
            oos.writeObject(graph);
            LOG.info("Graph saved to cache: " + cacheFile);
        } catch (IOException e) {
            LOG.warning("Could not save graph cache: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Graph construction
    // -------------------------------------------------------------------------

    /**
     * Builds a {@link ContactGraph} by scanning all user directories under
     * {@code datasetRoot}, reading only {@code sent} and {@code _sent_mail}
     * subfolders, deduplicating file content between the two folders per user,
     * and parsing each unique email file.
     *
     * <p>Files that cannot be read are counted as skipped and do not interrupt
     * the overall scan.</p>
     *
     * @param datasetRoot the root directory containing one subdirectory per user
     *                    (e.g., {@code data/maildir}).
     * @return the populated contact graph.
     * @throws IllegalArgumentException if {@code datasetRoot} is not an existing
     *                                  directory.
     */
    public ContactGraph buildGraph(Path datasetRoot) {
        if (!Files.isDirectory(datasetRoot)) {
            throw new IllegalArgumentException("Dataset root is not a directory: " + datasetRoot);
        }

        resetCounters();
        ContactGraph graph = new ContactGraph();

        // Each immediate subdirectory is one user's mailbox
        File[] userDirs = datasetRoot.toFile().listFiles(File::isDirectory);
        if (userDirs == null) return graph;

        Arrays.sort(userDirs); // deterministic ordering for reproducibility

        for (File userDir : userDirs) {
            processUserDirectory(userDir.toPath(), graph);
        }

        printReport();
        return graph;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Processes one user's mailbox: collects files from {@code sent} and
     * {@code _sent_mail}, deduplicates them, then parses and feeds each unique
     * message into the graph.
     *
     * @param userDir the user's mailbox directory.
     * @param graph   the graph being built.
     */
    private void processUserDirectory(Path userDir, ContactGraph graph) {
        // Collect all candidate files per target folder
        Map<String, List<Path>> folderFiles = new LinkedHashMap<>();
        for (String folderName : TARGET_FOLDERS) {
            Path subDir = userDir.resolve(folderName);
            if (Files.isDirectory(subDir)) {
                folderFiles.put(folderName, listEmailFiles(subDir));
            }
        }

        // Deduplicate across folders using content hashes
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

        // Parse and ingest each unique message
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
            // Register this sender as a mailbox owner (filters the From combo box)
            graph.addOwner(msg.getSender());
            for (String recipient : msg.getRecipients()) {
                graph.addEdge(msg.getSender(), recipient);
            }
            messagesValid++;
        }
    }

    /**
     * Lists all regular files in a directory (non-recursive — Enron sent folders
     * are flat: each file is one email).
     *
     * @param dir the directory to list.
     * @return sorted list of file paths.
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
     * Reads the entire content of a file as a UTF-8 string.
     *
     * @param file path to the file.
     * @return file content, or {@code null} if an I/O error occurs.
     */
    private String readFile(Path file) {
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Computes a hex-encoded SHA-256 hash of the file's content for deduplication.
     *
     * @param file path to the file.
     * @return hex hash string, or {@code null} on error.
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

    /** Resets all counters before a new scan. */
    private void resetCounters() {
        filesRead = filesSkipped = duplicatesDropped = messagesValid = 0;
    }

    /** Prints a scan summary to standard output. */
    private void printReport() {
        System.out.println("=== Dataset scan complete ===");
        System.out.println("  Files read       : " + filesRead);
        System.out.println("  Duplicates dropped: " + duplicatesDropped);
        System.out.println("  Files skipped    : " + filesSkipped);
        System.out.println("  Valid messages   : " + messagesValid);
    }
}
