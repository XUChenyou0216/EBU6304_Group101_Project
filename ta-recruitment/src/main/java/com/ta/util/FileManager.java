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

public class FileManager {
    private static final ConcurrentHashMap<String, ReentrantLock> FILE_LOCKS = new ConcurrentHashMap<>();

    @FunctionalInterface
    public interface RowFactory {
        String create(String generatedId);
    }

    @FunctionalInterface
    public interface RowTransformer {
        List<String> transform(List<String> rows);
    }

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

    public static void writeAll(String filePath, String csvHeader, List<String> rows) {
        try {
            withLockedFile(filePath, true, (raf, channel) -> {
                writeRows(channel, csvHeader, rows);
                return null;
            });
        } catch (IOException e) { e.printStackTrace(); }
    }

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

    public static void updateRows(String filePath, String csvHeader, RowTransformer transformer) {
        try {
            withLockedFile(filePath, true, (raf, channel) -> {
                List<String> rows = readRows(channel);
                writeRows(channel, csvHeader, transformer.transform(new ArrayList<>(rows)));
                return null;
            });
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static String generateNextId(String filePath, String prefix) {
        List<String> rows = readAll(filePath);
        return nextId(rows, prefix);
    }

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
