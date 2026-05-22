package com.ta;

import com.ta.dao.ApplicationDAO;
import com.ta.dao.UserDAO;
import com.ta.model.Application;
import com.ta.model.User;
import com.ta.util.PasswordUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Concurrency tests for CSV-backed DAO persistence under parallel access.
 *
 * <p>These tests exercise {@link ApplicationDAO} and {@link UserDAO} when
 * multiple threads perform simultaneous writes, updates, and duplicate-guarded
 * saves. They verify that generated identifiers remain unique, duplicate
 * applications are not persisted more than once, and concurrent updates do not
 * lose rows or corrupt stored data.</p>
 *
 * <p>Each test run uses an isolated data directory under {@code target/test-data-concurrency},
 * reset before every test method.</p>
 */
public class CsvConcurrencyTest {
    private static final String DATA_DIR = "target/test-data-concurrency";

    /**
     * Resets the shared test data directory before each test by deleting any
     * existing contents and recreating an empty directory.
     *
     * @throws IOException if the directory cannot be deleted or created
     */
    @Before
    public void resetDataDir() throws IOException {
        Path dataPath = Paths.get(DATA_DIR);
        if (Files.exists(dataPath)) {
            deleteRecursively(dataPath);
        }
        Files.createDirectories(dataPath);
    }

    /**
     * Verifies that concurrent application saves each receive a unique generated
     * application identifier and that all rows are persisted.
     *
     * @throws Exception if concurrent execution or DAO operations fail
     */
    @Test
    public void concurrentApplicationsGetUniqueIds() throws Exception {
        int threadCount = 32;
        ApplicationDAO dao = new ApplicationDAO(DATA_DIR);

        List<String> ids = runConcurrently(threadCount, index -> {
            Application app = new Application("", "TA" + index, "J100",
                    "SUBMITTED", "2026-05-10", "");
            String id = dao.saveWithNextId(app);
            assertNotNull(id);
            return id;
        });

        assertEquals(threadCount, ids.size());
        assertEquals(threadCount, new HashSet<>(ids).size());
        assertEquals(threadCount, dao.findAll().size());
    }

    /**
     * Verifies that concurrent duplicate application attempts for the same TA
     * and job result in exactly one persisted row.
     *
     * @throws Exception if concurrent execution or DAO operations fail
     */
    @Test
    public void concurrentDuplicateApplicationsOnlySaveOnce() throws Exception {
        int threadCount = 24;
        ApplicationDAO dao = new ApplicationDAO(DATA_DIR);

        List<Boolean> saved = runConcurrently(threadCount, index -> {
            Application app = new Application("", "TA_DUP", "J_DUP",
                    "SUBMITTED", "2026-05-10", "");
            return dao.saveIfNotApplied(app);
        });

        long successCount = saved.stream().filter(Boolean::booleanValue).count();
        assertEquals(1, successCount);
        assertEquals(1, dao.findByTa("TA_DUP").size());
    }

    /**
     * Verifies that concurrent user registrations each receive a unique user
     * identifier when saved through {@link UserDAO#saveIfUsernameAvailable(User)}.
     *
     * @throws Exception if concurrent execution or DAO operations fail
     */
    @Test
    public void concurrentUsersGetUniqueIds() throws Exception {
        int threadCount = 32;
        UserDAO dao = new UserDAO(DATA_DIR);

        List<String> ids = runConcurrently(threadCount, index -> {
            User user = new User("", "user_" + index, PasswordUtil.hash("pass123"),
                    "TA", "user_" + index + "@example.com", "pet", "dog", "ACTIVE");
            assertTrue(dao.saveIfUsernameAvailable(user));
            return user.getUserId();
        });

        assertEquals(threadCount, ids.size());
        assertEquals(threadCount, new HashSet<>(ids).size());
        assertEquals(threadCount, dao.findAll().size());
    }

    /**
     * Verifies that concurrent updates to distinct application rows do not
     * lose data and that each row retains its own review note after update.
     *
     * @throws Exception if concurrent execution or DAO operations fail
     */
    @Test
    public void concurrentUpdatesDoNotLoseRows() throws Exception {
        int threadCount = 16;
        ApplicationDAO dao = new ApplicationDAO(DATA_DIR);
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            Application app = new Application("", "TA" + i, "J_UPDATE",
                    "SUBMITTED", "2026-05-10", "");
            ids.add(dao.saveWithNextId(app));
        }

        runConcurrently(threadCount, index -> {
            Application app = dao.findById(ids.get(index));
            assertNotNull(app);
            app.setStatus("ACCEPTED");
            app.setReviewNote("review-" + index);
            dao.update(app);
            return true;
        });

        List<Application> applications = dao.findAll();
        assertEquals(threadCount, applications.size());
        assertEquals(threadCount, applications.stream()
                .filter(app -> "ACCEPTED".equals(app.getStatus()))
                .count());
        Set<String> reviewNotes = applications.stream()
                .map(Application::getReviewNote)
                .collect(Collectors.toSet());
        for (int i = 0; i < threadCount; i++) {
            assertTrue(reviewNotes.contains("review-" + i));
        }
    }

    private interface IndexedTask<T> {
        T run(int index) throws Exception;
    }

    private static <T> List<T> runConcurrently(int threadCount, IndexedTask<T> task) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<T>> futures = new ArrayList<>();
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            Callable<T> callable = () -> {
                start.await(5, TimeUnit.SECONDS);
                return task.run(index);
            };
            futures.add(executor.submit(callable));
        }

        start.countDown();
        List<T> results = new ArrayList<>();
        for (Future<T> future : futures) {
            results.add(future.get(10, TimeUnit.SECONDS));
        }
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        return results;
    }

    private static void deleteRecursively(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            /**
             * Deletes each visited file during recursive directory cleanup.
             *
             * @param file  path to the file being visited
             * @param attrs file attributes supplied by the directory walk
             * @return {@link FileVisitResult#CONTINUE} to proceed with remaining entries
             * @throws IOException if the file cannot be deleted
             */
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            /**
             * Deletes each directory after its contents have been visited.
             *
             * @param dir path to the directory being visited
             * @param exc I/O exception thrown while visiting entries in this directory, or {@code null}
             * @return {@link FileVisitResult#CONTINUE} to proceed with remaining directories
             * @throws IOException if the directory cannot be deleted or if {@code exc} is non-null
             */
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
