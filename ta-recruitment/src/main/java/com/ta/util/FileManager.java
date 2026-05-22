package com.ta.util;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Thread-safe utility for reading and writing CSV data files on disk.
 * <p>
 * All file operations acquire both a JVM-level {@link ReentrantLock} and an OS-level
 * {@link FileLock} to prevent concurrent corruption when multiple threads or processes
 * access the same file. CSV files are expected to have a header row followed by data rows;
 * the header is managed by callers and is not included in row lists returned by read methods.
 * </p>
 */
public class FileManager {
    private static final ConcurrentHashMap<String, ReentrantLock> FILE_LOCKS = new ConcurrentHashMap<>();

    /**
     * Functional interface for constructing a new CSV row when a generated identifier is available.
     *
     * @see #appendWithGeneratedId(String, String, String, RowFactory)
     */
    @FunctionalInterface
    public interface RowFactory {
        /**
         * Creates a CSV row string using the supplied generated identifier.
         *
         * @param generatedId the newly allocated row identifier (e.g. {@code "APP001"})
         * @return the complete CSV row to append, without a trailing line separator
         */
        String create(String generatedId);
    }

    /**
     * Functional interface for transforming the full list of CSV data rows in place.
     *
     * @see #updateRows(String, String, RowTransformer)
     */
    @FunctionalInterface
    public interface RowTransformer {
        /**
         * Transforms the current list of CSV data rows.
         *
         * @param rows a mutable copy of all data rows (header excluded); may be modified or replaced
         * @return the list of rows to persist back to the file
         */
        List<String> transform(List<String> rows);
    }

    /**
     * Reads all data rows from a CSV file, excluding the header row.
     * <p>
     * If the file does not exist, an empty list is returned. I/O errors are logged to
     * standard error and also result in an empty list.
     * </p>
     *
     * @param filePath absolute or relative path to the CSV file
     * @return a list of data row strings (never {@code null}); empty if the file is missing or unreadable
     */
    public static List<String> readAll(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) return new ArrayList<>();
        try {
            return withLockedFile(filePath, false, (raf, channel) -> readRows(channel));
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Appends a single data row to the end of a CSV file.
     * <p>
     * If the file is empty, the supplied header is written first, followed by the row.
     * A platform line separator is appended after the row.
     * </p>
     *
     * @param filePath   path to the CSV file (created if absent, along with parent directories)
     * @param csvHeader  the header line to write when the file is new or empty
     * @param row        the data row to append (without trailing line separator)
     */
    public static void appendRow(String filePath, String csvHeader, String row) {
        try {
            withLockedFile(filePath, true, (raf, channel) -> {
                boolean needsHeader = channel.size() == 0;
                channel.position(channel.size());
                writeLine(channel, needsHeader ? csvHeader + System.lineSeparator() + row : row);
                writeLineSeparator(channel);
                return null;
            });
        } catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * Replaces the entire contents of a CSV file with a new header and data rows.
     * <p>
     * Any existing content is truncated before writing. I/O errors are logged to standard error.
     * </p>
     *
     * @param filePath   path to the CSV file
     * @param csvHeader  the header line to write as the first line
     * @param rows       the complete list of data rows to persist (header excluded)
     */
    public static void writeAll(String filePath, String csvHeader, List<String> rows) {
        try {
            withLockedFile(filePath, true, (raf, channel) -> {
                writeRows(channel, csvHeader, rows);
                return null;
            });
        } catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * Reads existing rows, generates the next sequential identifier, appends a new row,
     * and rewrites the file atomically under lock.
     *
     * @param filePath   path to the CSV file
     * @param csvHeader  the header line used when rewriting the file
     * @param prefix     identifier prefix used by {@link #nextId(List, String)} (e.g. {@code "APP"})
     * @param rowFactory callback that builds the new row from the generated identifier
     * @return the generated identifier on success, or {@code null} if an I/O error occurs
     */
    public static String appendWithGeneratedId(String filePath, String csvHeader, String prefix, RowFactory rowFactory) {
        try {
            return withLockedFile(filePath, true, (raf, channel) -> {
                List<String> rows = readRows(channel);
                String generatedId = nextId(rows, prefix);
                rows.add(rowFactory.create(generatedId));
                writeRows(channel, csvHeader, rows);
                return generatedId;
            });
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Appends a new row only if no existing row satisfies the given existence predicate.
     * <p>
     * The entire read-check-write cycle is performed under an exclusive file lock.
     * </p>
     *
     * @param filePath         path to the CSV file
     * @param csvHeader        the header line used when rewriting the file
     * @param existsPredicate  returns {@code true} for a row that should prevent insertion
     * @param rowFactory       produces the new row given the current row list (including any
     *                         rows already present)
     * @return {@code true} if a new row was appended, {@code false} if a matching row already
     *         exists or an I/O error occurs
     */
    public static boolean appendIfAbsent(String filePath, String csvHeader,
                                         Predicate<String> existsPredicate,
                                         Function<List<String>, String> rowFactory) {
        try {
            return withLockedFile(filePath, true, (raf, channel) -> {
                List<String> rows = readRows(channel);
                if (rows.stream().anyMatch(existsPredicate)) {
                    return false;
                }
                rows.add(rowFactory.apply(new ArrayList<>(rows)));
                writeRows(channel, csvHeader, rows);
                return true;
            });
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Reads all data rows, applies a transformation, and writes the result back to the file.
     *
     * @param filePath    path to the CSV file
     * @param csvHeader   the header line used when rewriting the file
     * @param transformer callback that receives a copy of the current rows and returns the
     *                    updated list to persist
     */
    public static void updateRows(String filePath, String csvHeader, RowTransformer transformer) {
        try {
            withLockedFile(filePath, true, (raf, channel) -> {
                List<String> rows = readRows(channel);
                writeRows(channel, csvHeader, transformer.transform(new ArrayList<>(rows)));
                return null;
            });
        } catch (IOException e) { e.printStackTrace(); }
    }

    /**
     * Generates the next sequential identifier by scanning all rows in the given file.
     *
     * @param filePath path to the CSV file whose rows are scanned
     * @param prefix   identifier prefix (e.g. {@code "JOB"})
     * @return the next identifier in the format {@code prefix + zero-padded 3-digit number}
     * @see #nextId(List, String)
     */
    public static String generateNextId(String filePath, String prefix) {
        List<String> rows = readAll(filePath);
        return nextId(rows, prefix);
    }

    /**
     * Computes the next sequential identifier from an in-memory list of CSV rows.
     * <p>
     * Each row's first comma-separated field is inspected. Fields starting with {@code prefix}
     * followed by a numeric suffix contribute to the maximum; the returned value is
     * {@code prefix} concatenated with {@code (max + 1)} formatted as three digits
     * (e.g. {@code "APP004"}).
     * </p>
     *
     * @param rows   list of CSV data rows (header excluded)
     * @param prefix the identifier prefix to match and prepend
     * @return the next identifier string
     */
    public static String nextId(List<String> rows, String prefix) {
        int maxNum = 0;
        for (String row : rows) {
            String id = row.split(",", 2)[0];
            if (id.startsWith(prefix)) {
                try {
                    int num = Integer.parseInt(id.substring(prefix.length()));
                    if (num > maxNum) maxNum = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("%s%03d", prefix, maxNum + 1);
    }

    private static <T> T withLockedFile(String filePath, boolean exclusive, LockedFileOperation<T> operation)
            throws IOException {
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null) parent.mkdirs();

        String lockKey = canonicalLockKey(filePath);
        ReentrantLock jvmLock = FILE_LOCKS.computeIfAbsent(lockKey, key -> new ReentrantLock());
        jvmLock.lock();
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw");
             FileChannel channel = raf.getChannel();
             FileLock ignored = channel.lock(0L, Long.MAX_VALUE, !exclusive)) {
            return operation.run(raf, channel);
        } finally {
            jvmLock.unlock();
        }
    }

    private static String canonicalLockKey(String filePath) {
        try {
            return Paths.get(filePath).toAbsolutePath().normalize().toFile().getCanonicalPath();
        } catch (IOException e) {
            Path path = Paths.get(filePath).toAbsolutePath().normalize();
            return path.toString();
        }
    }

    private static List<String> readRows(FileChannel channel) throws IOException {
        List<String> rows = new ArrayList<>();
        String content = readContent(channel);
        if (content.isEmpty()) return rows;

        try (BufferedReader br = new BufferedReader(new StringReader(content))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) { isHeader = false; continue; }
                if (!line.trim().isEmpty()) rows.add(line);
            }
        }
        return rows;
    }

    private static String readContent(FileChannel channel) throws IOException {
        channel.position(0);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteBuffer buffer = ByteBuffer.allocate(8192);
        while (channel.read(buffer) != -1) {
            buffer.flip();
            while (buffer.hasRemaining()) {
                out.write(buffer.get());
            }
            buffer.clear();
        }
        return out.toString(StandardCharsets.UTF_8.name());
    }

    private static void writeRows(FileChannel channel, String csvHeader, List<String> rows) throws IOException {
        channel.truncate(0);
        channel.position(0);
        writeLine(channel, csvHeader);
        writeLineSeparator(channel);
        for (String row : rows) {
            writeLine(channel, row);
            writeLineSeparator(channel);
        }
        channel.force(true);
    }

    private static void writeLine(FileChannel channel, String value) throws IOException {
        ByteBuffer buffer = StandardCharsets.UTF_8.encode(value);
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    private static void writeLineSeparator(FileChannel channel) throws IOException {
        writeLine(channel, System.lineSeparator());
    }

    @FunctionalInterface
    private interface LockedFileOperation<T> {
        T run(RandomAccessFile raf, FileChannel channel) throws IOException;
    }
}
