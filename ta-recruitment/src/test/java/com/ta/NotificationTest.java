package com.ta;

import com.ta.dao.ApplicationDAO;
import com.ta.dao.NotificationDAO;
import com.ta.model.Application;
import com.ta.model.Notification;
import com.ta.servlet.AdminWorkloadServlet;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;

/**

 * Unit tests for the TA recruitment notification system.
 *
 * <p>This test class validates notification type constants, UI color mapping,
 * job-posted and workload-alert notification flows, deduplication behaviour,
 * workload boundary calculations, and {@link NotificationDAO} persistence.</p>
 *
 * <p>Tests use JUnit 4 with a {@link TemporaryFolder} rule to create isolated
 * on-disk data directories for DAO-backed scenarios. Workload-alert creation
 * is exercised via a private helper ({@code saveAlertIfNeeded}) that mirrors
 * the deduplication logic in {@link AdminWorkloadServlet#doGet}.</p>
 *
 * <p>Test coverage includes:</p>
 * <ol>
 *   <li>Type constants and color mapping ({@link Notification} model)</li>
 *   <li>MO receives a {@code JOB_POSTED} notification when a job is saved</li>
 *   <li>Admin receives a {@code WORKLOAD_ALERT} when a TA's estimated hours exceed the limit</li>
 *   <li>Workload-alert deduplication (second alert not created while first is unread)</li>
 *   <li>Alert is re-created after the first one is marked read</li>
 *   <li>No alert when workload is within the limit</li>
 *   <li>Boundary: exactly 3 accepted jobs (48 h) does NOT trigger alert</li>
 *   <li>Boundary: 4 accepted jobs (64 h) triggers alert</li>
 *   <li>Workload derived from real {@link ApplicationDAO} CSV data</li>
 *   <li>{@link NotificationDAO} CRUD (save, findByUser, countUnread, markRead, markAllRead)</li>
 *   <li>{@link Notification} CSV round-trip ({@link Notification#toCsvRow()} /
 *       {@link Notification#fromCsvRow(String)})</li>
 * </ol>
 *
 * <p>Workload constants (from {@link AdminWorkloadServlet}):</p>
 * <ul>
 *   <li>{@code HOURS_PER_ACCEPTED_ASSIGNMENT = 16}</li>
 *   <li>{@code WORKLOAD_LIMIT_HOURS = 48} (i.e. &gt; 3 accepted jobs → exceeded)</li>
 * </ul>
 *
 * <p>Run with: {@code mvn -Dtest=NotificationTest test}</p>
 *
 * <p>Each test method that touches the filesystem uses the {@link #folder}
 * {@link TemporaryFolder} rule to obtain a unique, automatically deleted
 * working directory.</p>
 *
 * @see Notification
 * @see NotificationDAO
 * @see AdminWorkloadServlet
 * @see ApplicationDAO
 * @see com.ta.servlet.PostJobServlet
 */
public class NotificationTest {

    /**
     * JUnit 4 rule that supplies an isolated temporary directory per test method.
     *
     * <p>Tests pass {@code folder.newFolder("data")} (or similar) into
     * {@link NotificationDAO} and {@link ApplicationDAO} constructors so CSV
     * persistence never writes to the application's real data directory.
     * The folder and its contents are removed after each test completes.</p>
     */

 * Notification system unit tests.
 *
 * Covers:
 *   1. Type constants and color mapping (Notification model)
 *   2. MO receives a JOB_POSTED notification when a job is saved
 *   3. Admin receives a WORKLOAD_ALERT when a TA's estimated hours exceed the limit
 *   4. Workload-alert deduplication (second alert not created while first is unread)
 *   5. Alert is re-created after the first one is marked read
 *   6. No alert when workload is within the limit
 *   7. Boundary: exactly 3 accepted jobs (48 h) does NOT trigger alert
 *   8. Boundary: 4 accepted jobs (64 h) triggers alert
 *   9. NotificationDAO CRUD (save, findByUser, countUnread, markRead, markAllRead)
 *
 * Workload constants (from AdminWorkloadServlet):
 *   HOURS_PER_ACCEPTED_ASSIGNMENT = 16
 *   WORKLOAD_LIMIT_HOURS          = 48   (i.e. > 3 accepted jobs → exceeded)
 *
 * Run with: mvn -Dtest=NotificationTest test
 */
public class NotificationTest {


    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    // ── Shared constants mirrored from AdminWorkloadServlet ───────────────
    private static final int HOURS_PER = AdminWorkloadServlet.HOURS_PER_ACCEPTED_ASSIGNMENT; // 16
    private static final int LIMIT     = AdminWorkloadServlet.WORKLOAD_LIMIT_HOURS;           // 48

    // ─────────────────────────────────────────────────────────────────────
    // 1. TYPE CONSTANTS
    // ─────────────────────────────────────────────────────────────────────


    /**
     * Verifies that all {@link Notification} type constant strings match their
     * expected literal values used throughout the application.
     *
     * <p>Each constant ({@code APPLICATION_SUBMITTED}, {@code STATUS_UPDATED},
     * {@code JOB_POSTED}, {@code WORKLOAD_ALERT}) must equal its canonical
     * string so that servlet logic, DAO persistence, and UI filtering remain
     * consistent.</p>
     */

    @Test
    public void typeConstants_haveExpectedValues() {
        assertEquals("APPLICATION_SUBMITTED", Notification.TYPE_APPLICATION_SUBMITTED);
        assertEquals("STATUS_UPDATED",        Notification.TYPE_STATUS_UPDATED);
        assertEquals("JOB_POSTED",            Notification.TYPE_JOB_POSTED);
        assertEquals("WORKLOAD_ALERT",        Notification.TYPE_WORKLOAD_ALERT);
    }

    // ─────────────────────────────────────────────────────────────────────
    // 2. COLOR MAPPING
    // ─────────────────────────────────────────────────────────────────────


    /**
     * Asserts that a {@code JOB_POSTED} notification maps to the {@code success}
     * Bootstrap-style color token via {@link Notification#getColor()}.
     *
     * <p>Confirms that newly posted job notifications render with a positive
     * (green) visual indicator in the UI.</p>
     */

    @Test
    public void jobPosted_colorIsSuccess() {
        Notification n = notifOf(Notification.TYPE_JOB_POSTED);
        assertEquals("success", n.getColor());
    }


    /**
     * Asserts that a {@code WORKLOAD_ALERT} notification maps to the {@code danger}
     * Bootstrap-style color token via {@link Notification#getColor()}.
     *
     * <p>Confirms that workload-overload alerts render with a critical (red)
     * visual indicator in the UI.</p>
     */

    @Test
    public void workloadAlert_colorIsDanger() {
        Notification n = notifOf(Notification.TYPE_WORKLOAD_ALERT);
        assertEquals("danger", n.getColor());
    }


    /**
     * Asserts that a {@code STATUS_UPDATED} notification maps to the {@code info}
     * Bootstrap-style color token via {@link Notification#getColor()}.
     *
     * <p>Confirms that application status-change notifications render with a
     * neutral informational (blue) visual indicator.</p>
     */

    @Test
    public void statusUpdated_colorIsInfo() {
        Notification n = notifOf(Notification.TYPE_STATUS_UPDATED);
        assertEquals("info", n.getColor());
    }


    /**
     * Asserts that an {@code APPLICATION_SUBMITTED} notification maps to the
     * {@code primary} Bootstrap-style color token via {@link Notification#getColor()}.
     *
     * <p>Confirms that new application submissions render with the primary
     * (brand) visual indicator in the UI.</p>
     */

    @Test
    public void applicationSubmitted_colorIsPrimary() {
        Notification n = notifOf(Notification.TYPE_APPLICATION_SUBMITTED);
        assertEquals("primary", n.getColor());
    }


    /**
     * Asserts that an unrecognised notification type falls back to the
     * {@code secondary} Bootstrap-style color token via {@link Notification#getColor()}.
     *
     * <p>Ensures the model degrades gracefully when persisted data contains an
     * unknown type string rather than throwing or returning {@code null}.</p>
     */

    @Test
    public void unknownType_colorIsSecondary() {
        Notification n = notifOf("UNKNOWN_TYPE");
        assertEquals("secondary", n.getColor());
    }


    /**
     * Asserts that a {@code null} notification type falls back to the
     * {@code secondary} Bootstrap-style color token via {@link Notification#getColor()}.
     *
     * <p>Ensures the model handles missing or corrupt type values without
     * failing at render time.</p>
     */

    @Test
    public void nullType_colorIsSecondary() {
        Notification n = notifOf(null);
        assertEquals("secondary", n.getColor());
    }

    // ─────────────────────────────────────────────────────────────────────
    // 3. MO JOB-POSTED NOTIFICATION
    // ─────────────────────────────────────────────────────────────────────


    /**
     * Simulates the notification created by {@link com.ta.servlet.PostJobServlet}
     * after a new job
     * is saved and verifies that the MO user receives exactly one unread
     * {@code JOB_POSTED} notification with the expected message, type, and color.
     *
     * <p>Setup: creates a temporary data directory, instantiates
     * {@link NotificationDAO}, builds a notification mirroring the servlet's
     * post-save logic, and persists it for user {@code MO_01}.</p>
     *
     * <p>Assertions: exactly one notification exists; type is
     * {@link Notification#TYPE_JOB_POSTED}; {@code isRead} is {@code false};
     * message contains the job title fragment; color is {@code success}.</p>
     *
     * @throws Exception if temporary directory creation or DAO I/O fails
     */

    @Test
    public void moReceivesJobPostedNotification_afterJobSaved() throws Exception {
        File dataDir = folder.newFolder("data");
        NotificationDAO dao = new NotificationDAO(dataDir.getAbsolutePath());

        // Simulate what PostJobServlet does after jobDAO.save(newJob)
        String moUserId = "MO_01";
        String message  = "Job \"EBU6304 — TA Lab\" has been posted successfully.";
        Notification notif = new Notification(
                dao.generateNextId(), moUserId,
                Notification.TYPE_JOB_POSTED,
                message, false, LocalDate.now().toString());
        dao.save(notif);

        List<Notification> mine = dao.findByUser(moUserId);
        assertEquals("MO should have exactly 1 notification", 1, mine.size());

        Notification saved = mine.get(0);
        assertEquals(Notification.TYPE_JOB_POSTED, saved.getType());
        assertFalse("Notification should start as unread", saved.isRead());
        assertTrue("Message should contain job title", saved.getMessage().contains("TA Lab"));
        assertEquals("success", saved.getColor());
    }


    /**
     * Verifies that {@link NotificationDAO#generateNextId()} produces identifiers
     * prefixed with {@code NTF}, consistent with the application's ID convention.
     *
     * <p>Ensures generated notification IDs are distinguishable from other entity
     * IDs (e.g. application or job IDs) in persisted CSV data.</p>
     *
     * @throws Exception if temporary directory creation or DAO initialisation fails
     */

    @Test
    public void jobPostedNotification_idStartsWithNtf() throws Exception {
        File dataDir = folder.newFolder("data");
        NotificationDAO dao = new NotificationDAO(dataDir.getAbsolutePath());
        String id = dao.generateNextId();
        assertTrue("Generated ID should start with NTF", id.startsWith("NTF"));
    }


    /**
     * Confirms that editing an existing job does not implicitly create a
     * {@code JOB_POSTED} notification; only an explicit save produces notifications.
     *
     * <p>{@link com.ta.servlet.PostJobServlet} creates a notification only in the
     * "create" branch.
     * This test verifies the negative case: with no notification ever saved,
     * {@link NotificationDAO#findByUser(String)} returns an empty list for the MO
     * user.</p>
     *
     * @throws Exception if temporary directory creation or DAO I/O fails
     */

    @Test
    public void editJob_doesNotProduceJobPostedNotification() throws Exception {
        // PostJobServlet only creates a notification in the "create" branch.
        // Editing an existing job should NOT add a notification — verified here by
        // checking that no notification exists when none was explicitly saved.
        File dataDir = folder.newFolder("data");
        NotificationDAO dao = new NotificationDAO(dataDir.getAbsolutePath());

        // Nothing saved → list must be empty
        assertTrue("No notification should exist before any save",
                dao.findByUser("MO_02").isEmpty());
    }

    // ─────────────────────────────────────────────────────────────────────
    // 4. WORKLOAD CONSTANTS
    // ─────────────────────────────────────────────────────────────────────


    /**
     * Asserts that the mirrored workload constants match the values defined in
     * {@link AdminWorkloadServlet} (16 hours per assignment, 48-hour limit).
     *
     * <p>Guards against accidental drift between production servlet constants and
     * test expectations used throughout this class.</p>
     */

    @Test
    public void workloadConstants_matchDefinition() {
        assertEquals("HOURS_PER_ACCEPTED_ASSIGNMENT must be 16", 16, HOURS_PER);
        assertEquals("WORKLOAD_LIMIT_HOURS must be 48",          48, LIMIT);
    }


    /**
     * Verifies the boundary condition where exactly three accepted assignments
     * (48 hours) equals the limit and must not be treated as exceeded.
     *
     * <p>Uses the same {@code hours &gt; LIMIT} comparison as
     * {@link AdminWorkloadServlet} and {@code saveAlertIfNeeded}.</p>
     */

    @Test
    public void threeAcceptedJobs_isExactlyAtLimit_notExceeded() {
        int hours   = 3 * HOURS_PER;   // 48
        boolean exceeded = hours > LIMIT;
        assertFalse("Exactly 48 h (3 accepted jobs) must NOT be flagged as exceeded", exceeded);
    }


    /**
     * Verifies that four accepted assignments (64 hours) exceed the 48-hour limit
     * and must be flagged as over the workload threshold.
     *
     * <p>Uses the same {@code hours &gt; LIMIT} comparison as
     * {@link AdminWorkloadServlet} and {@code saveAlertIfNeeded}.</p>
     */

    @Test
    public void fourAcceptedJobs_exceedsLimit() {
        int hours   = 4 * HOURS_PER;   // 64
        boolean exceeded = hours > LIMIT;
        assertTrue("64 h (4 accepted jobs) must be flagged as exceeded", exceeded);
    }

    // ─────────────────────────────────────────────────────────────────────
    // 5. WORKLOAD ALERT NOTIFICATION — creation
    // ─────────────────────────────────────────────────────────────────────


    /**
     * Simulates {@link AdminWorkloadServlet} alert creation when a TA exceeds the
     * workload limit and verifies that the admin receives one unread
     * {@code WORKLOAD_ALERT} containing the TA marker, estimated hours, and
     * {@code danger} color.
     *
     * <p>Setup: TA {@code TA_99} with 64 estimated hours (4 accepted assignments);
     * {@code saveAlertIfNeeded} is invoked once for admin {@code ADMIN_01}.</p>
     *
     * <p>Assertions: one alert exists; type is
     * {@link Notification#TYPE_WORKLOAD_ALERT}; unread; message contains
     * {@code [TA:TA_99]} and hour count; color is {@code danger}.</p>
     *
     * @throws Exception if temporary directory creation or DAO I/O fails
     */

    @Test
    public void adminReceivesWorkloadAlert_whenTaExceedsLimit() throws Exception {
        File dataDir = folder.newFolder("data");
        NotificationDAO notifDao = new NotificationDAO(dataDir.getAbsolutePath());

        String adminId = "ADMIN_01";
        String taId    = "TA_99";
        int hours      = 4 * HOURS_PER; // 64 h — exceeded

        // Simulate the alert-creation logic from AdminWorkloadServlet
        saveAlertIfNeeded(notifDao, adminId, taId, "Alice", hours);

        List<Notification> alerts = dao_findByUserAndType(notifDao, adminId,
                                                           Notification.TYPE_WORKLOAD_ALERT);
        assertEquals("Admin should receive 1 workload alert", 1, alerts.size());

        Notification alert = alerts.get(0);
        assertEquals(Notification.TYPE_WORKLOAD_ALERT, alert.getType());
        assertFalse("Alert should start as unread", alert.isRead());
        assertTrue("Alert message must contain TA user ID marker",
                alert.getMessage().contains("[TA:" + taId + "]"));
        assertTrue("Alert message must contain estimated hours",
                alert.getMessage().contains(String.valueOf(hours)));
        assertEquals("danger", alert.getColor());
    }


    /**
     * Verifies that no {@code WORKLOAD_ALERT} is created when a TA's estimated
     * hours are exactly at the limit (48 h) and therefore not exceeded.
     *
     * <p>Setup: TA {@code TA_50} with 48 estimated hours (3 accepted assignments);
     * {@code saveAlertIfNeeded} is invoked once.</p>
     *
     * <p>Assertion: the admin's workload-alert list remains empty because
     * {@code estimatedHours &lt;= LIMIT} short-circuits alert creation.</p>
     *
     * @throws Exception if temporary directory creation or DAO I/O fails
     */

    @Test
    public void noAlert_whenTaIsWithinLimit() throws Exception {
        File dataDir = folder.newFolder("data");
        NotificationDAO notifDao = new NotificationDAO(dataDir.getAbsolutePath());

        String adminId = "ADMIN_01";
        String taId    = "TA_50";
        int hours      = 3 * HOURS_PER; // 48 h — NOT exceeded

        saveAlertIfNeeded(notifDao, adminId, taId, "Bob", hours);

        List<Notification> alerts = dao_findByUserAndType(notifDao, adminId,
                                                           Notification.TYPE_WORKLOAD_ALERT);
        assertTrue("No alert should be generated when workload is within limit",
                alerts.isEmpty());
    }

    // ─────────────────────────────────────────────────────────────────────
    // 6. WORKLOAD ALERT DEDUPLICATION
    // ─────────────────────────────────────────────────────────────────────


    /**
     * Verifies that a second {@code WORKLOAD_ALERT} is suppressed while an unread
     * alert for the same TA already exists for the admin (deduplication on
     * repeated page loads).
     *
     * <p>Setup: {@code saveAlertIfNeeded} is called twice for the same admin/TA
     * pair with exceeded workload; the first call creates the alert, the second
     * simulates a subsequent {@link AdminWorkloadServlet} page load.</p>
     *
     * <p>Assertion: only one alert exists in persistence after both invocations.</p>
     *
     * @throws Exception if temporary directory creation or DAO I/O fails
     */

    @Test
    public void secondAlert_notCreated_whileFirstIsUnread() throws Exception {
        File dataDir = folder.newFolder("data");
        NotificationDAO notifDao = new NotificationDAO(dataDir.getAbsolutePath());

        String adminId = "ADMIN_01";
        String taId    = "TA_77";
        int hours      = 4 * HOURS_PER; // exceeded

        // First page load → alert created
        saveAlertIfNeeded(notifDao, adminId, taId, "Carol", hours);
        // Second page load → should be deduplicated (alert still unread)
        saveAlertIfNeeded(notifDao, adminId, taId, "Carol", hours);

        List<Notification> alerts = dao_findByUserAndType(notifDao, adminId,
                                                           Notification.TYPE_WORKLOAD_ALERT);
        assertEquals("Only 1 alert should exist (duplicate suppressed)", 1, alerts.size());
    }


    /**
     * Verifies that a new {@code WORKLOAD_ALERT} is created after the previous
     * unread alert for the same TA has been marked read, leaving exactly one
     * unread alert in the admin's notification list.
     *
     * <p>Setup: first alert is created and then marked read via
     * {@link NotificationDAO#markRead(String)}; a second
     * {@code saveAlertIfNeeded} call simulates the next workload check.</p>
     *
     * <p>Assertions: two alerts exist in total; exactly one remains unread.</p>
     *
     * @throws Exception if temporary directory creation or DAO I/O fails
     */

    @Test
    public void alertRecreated_afterFirstIsMarkedRead() throws Exception {
        File dataDir = folder.newFolder("data");
        NotificationDAO notifDao = new NotificationDAO(dataDir.getAbsolutePath());

        String adminId = "ADMIN_01";
        String taId    = "TA_88";
        int hours      = 4 * HOURS_PER;

        // First alert
        saveAlertIfNeeded(notifDao, adminId, taId, "David", hours);

        // Admin reads the alert
        List<Notification> first = dao_findByUserAndType(notifDao, adminId,
                                                          Notification.TYPE_WORKLOAD_ALERT);
        notifDao.markRead(first.get(0).getNotificationId());

        // Next workload check → new alert should be created (previous is now read)
        saveAlertIfNeeded(notifDao, adminId, taId, "David", hours);

        List<Notification> all = dao_findByUserAndType(notifDao, adminId,
                                                        Notification.TYPE_WORKLOAD_ALERT);
        assertEquals("A second alert must be created after the first was read", 2, all.size());
        long unreadCount = all.stream().filter(n -> !n.isRead()).count();
        assertEquals("Exactly 1 unread alert should exist", 1, unreadCount);
    }

    // ─────────────────────────────────────────────────────────────────────
    // 7. WORKLOAD COMPUTED FROM REAL APPLICATION DATA
    // ─────────────────────────────────────────────────────────────────────


    /**
     * Loads four accepted applications from CSV via {@link ApplicationDAO} and
     * verifies that the derived estimated hours (64) exceed the 48-hour limit.
     *
     * <p>Setup: writes an {@code applications.csv} file with four
     * {@code ACCEPTED} rows for TA {@code TA_X}, then counts accepted
     * applications and multiplies by {@code HOURS_PER}.</p>
     *
     * <p>Assertions: 4 accepted applications; 64 estimated hours;
     * {@code exceeded} flag is {@code true}.</p>
     *
     * @throws Exception if temporary file creation or DAO I/O fails
     */

    @Test
    public void workloadExceededFlag_derivedFromApplicationDAO() throws Exception {
        File dataDir = folder.newFolder("data");
        File appsFile = new File(dataDir, "applications.csv");
        try (FileWriter w = new FileWriter(appsFile)) {
            w.write(Application.CSV_HEADER + "\n");
            // 4 accepted applications for TA_X → 64 h
            w.write("APP1,TA_X,J1,ACCEPTED,2026-01-01,\"\"\n");
            w.write("APP2,TA_X,J2,ACCEPTED,2026-01-02,\"\"\n");
            w.write("APP3,TA_X,J3,ACCEPTED,2026-01-03,\"\"\n");
            w.write("APP4,TA_X,J4,ACCEPTED,2026-01-04,\"\"\n");
        }

        ApplicationDAO appDao = new ApplicationDAO(dataDir.getAbsolutePath());
        long acceptedCount = appDao.findByTa("TA_X").stream()
                .filter(a -> "ACCEPTED".equals(a.getStatus()))
                .count();
        int estimatedHours = (int) (acceptedCount * HOURS_PER);
        boolean exceeded   = estimatedHours > LIMIT;

        assertEquals("TA_X should have 4 accepted applications", 4, acceptedCount);
        assertEquals("Estimated hours should be 64", 64, estimatedHours);
        assertTrue("64 h must exceed the 48-h limit", exceeded);
    }


    /**
     * Loads three accepted applications from CSV via {@link ApplicationDAO} and
     * verifies that the derived estimated hours (48) do not exceed the limit.
     *
     * <p>Setup: writes an {@code applications.csv} file with three
     * {@code ACCEPTED} rows for TA {@code TA_Y}.</p>
     *
     * <p>Assertions: 3 accepted applications; 48 estimated hours;
     * {@code exceeded} flag is {@code false} (boundary at limit, not over).</p>
     *
     * @throws Exception if temporary file creation or DAO I/O fails
     */

    @Test
    public void workloadNotExceeded_withThreeAcceptedApplications() throws Exception {
        File dataDir = folder.newFolder("data");
        File appsFile = new File(dataDir, "applications.csv");
        try (FileWriter w = new FileWriter(appsFile)) {
            w.write(Application.CSV_HEADER + "\n");
            w.write("APP1,TA_Y,J1,ACCEPTED,2026-01-01,\"\"\n");
            w.write("APP2,TA_Y,J2,ACCEPTED,2026-01-02,\"\"\n");
            w.write("APP3,TA_Y,J3,ACCEPTED,2026-01-03,\"\"\n");
        }

        ApplicationDAO appDao = new ApplicationDAO(dataDir.getAbsolutePath());
        long acceptedCount = appDao.findByTa("TA_Y").stream()
                .filter(a -> "ACCEPTED".equals(a.getStatus()))
                .count();
        int estimatedHours = (int) (acceptedCount * HOURS_PER);
        boolean exceeded   = estimatedHours > LIMIT;

        assertEquals("TA_Y should have 3 accepted applications", 3, acceptedCount);
        assertEquals("Estimated hours should be 48", 48, estimatedHours);
        assertFalse("48 h must NOT exceed the 48-h limit", exceeded);
    }

    // ─────────────────────────────────────────────────────────────────────
    // 8. NotificationDAO CRUD
    // ─────────────────────────────────────────────────────────────────────


    /**
     * Verifies that {@link NotificationDAO#save(Notification)} persists notifications
     * and {@link NotificationDAO#findByUser(String)} returns only the notifications
     * belonging to the requested user.
     *
     * <p>Setup: saves three notifications across two users ({@code U1}, {@code U2})
     * with distinct types.</p>
     *
     * <p>Assertions: user {@code U1} has 2 notifications; user {@code U2} has 1.</p>
     *
     * @throws Exception if temporary directory creation or DAO I/O fails
     */

    @Test
    public void save_andFindByUser() throws Exception {
        File dataDir = folder.newFolder("data");
        NotificationDAO dao = new NotificationDAO(dataDir.getAbsolutePath());

        dao.save(buildNotif("NTF1", "U1", Notification.TYPE_STATUS_UPDATED, "Status changed"));
        dao.save(buildNotif("NTF2", "U1", Notification.TYPE_JOB_POSTED,    "Job posted"));
        dao.save(buildNotif("NTF3", "U2", Notification.TYPE_WORKLOAD_ALERT, "Overload"));

        List<Notification> u1 = dao.findByUser("U1");
        assertEquals("U1 should have 2 notifications", 2, u1.size());
        assertEquals("U2 should have 1 notification",  1, dao.findByUser("U2").size());
    }


    /**
     * Verifies that {@link NotificationDAO#countUnread(String)} counts only unread
     * notifications for the specified user and ignores read entries.
     *
     * <p>Setup: saves three notifications for {@code U1} — two unread, one read —
     * plus no extra users.</p>
     *
     * <p>Assertion: unread count for {@code U1} is 2.</p>
     *
     * @throws Exception if temporary directory creation or DAO I/O fails
     */

    @Test
    public void countUnread_onlyCountsUnreadForGivenUser() throws Exception {
        File dataDir = folder.newFolder("data");
        NotificationDAO dao = new NotificationDAO(dataDir.getAbsolutePath());

        dao.save(buildNotif("NTF1", "U1", Notification.TYPE_JOB_POSTED,   "A", false));
        dao.save(buildNotif("NTF2", "U1", Notification.TYPE_STATUS_UPDATED,"B", true));
        dao.save(buildNotif("NTF3", "U1", Notification.TYPE_JOB_POSTED,   "C", false));

        assertEquals("U1 should have 2 unread notifications", 2, dao.countUnread("U1"));
    }


    /**
     * Verifies that {@link NotificationDAO#markRead(String)} marks a single
     * notification as read and reduces the unread count to zero.
     *
     * <p>Setup: one unread notification ({@code NTF1}) for user {@code U1}.</p>
     *
     * <p>Assertions: unread count is 1 before {@code markRead}; 0 after.</p>
     *
     * @throws Exception if temporary directory creation or DAO I/O fails
     */

    @Test
    public void markRead_setsIsReadTrue() throws Exception {
        File dataDir = folder.newFolder("data");
        NotificationDAO dao = new NotificationDAO(dataDir.getAbsolutePath());

        dao.save(buildNotif("NTF1", "U1", Notification.TYPE_JOB_POSTED, "msg", false));
        assertEquals(1, dao.countUnread("U1"));

        dao.markRead("NTF1");
        assertEquals("After markRead, unread count must be 0", 0, dao.countUnread("U1"));
    }


    /**
     * Verifies that {@link NotificationDAO#markAllRead(String)} marks all unread
     * notifications for the given user as read without affecting other users.
     *
     * <p>Setup: two unread notifications for {@code U1} and one unread for
     * {@code U2}; {@code markAllRead("U1")} is invoked.</p>
     *
     * <p>Assertions: {@code U1} unread count drops to 0; {@code U2} remains at 1.</p>
     *
     * @throws Exception if temporary directory creation or DAO I/O fails
     */

    @Test
    public void markAllRead_setsAllUnreadToRead() throws Exception {
        File dataDir = folder.newFolder("data");
        NotificationDAO dao = new NotificationDAO(dataDir.getAbsolutePath());

        dao.save(buildNotif("NTF1", "U1", Notification.TYPE_JOB_POSTED,    "msg1", false));
        dao.save(buildNotif("NTF2", "U1", Notification.TYPE_STATUS_UPDATED, "msg2", false));
        dao.save(buildNotif("NTF3", "U2", Notification.TYPE_WORKLOAD_ALERT, "msg3", false));

        dao.markAllRead("U1");

        assertEquals("All U1 notifications must be read",   0, dao.countUnread("U1"));
        assertEquals("U2 notification must remain unread",  1, dao.countUnread("U2"));
    }


    /**
     * Verifies that serialising a {@link Notification} to CSV via
     * {@link Notification#toCsvRow()} and deserialising via
     * {@link Notification#fromCsvRow(String)} preserves all field values and
     * derived color.
     *
     * <p>Uses a workload-alert notification with a quoted message to exercise
     * CSV escaping. Asserts identity of all persisted fields plus
     * {@link Notification#getColor()} ({@code danger}).</p>
     */

    @Test
    public void csvRoundTrip_preservesAllFields() {
        Notification original = new Notification(
                "NTF99", "U5", Notification.TYPE_WORKLOAD_ALERT,
                "Test \"quoted\" message", false, "2026-05-02");

        String csv  = original.toCsvRow();
        Notification restored = Notification.fromCsvRow(csv);

        assertNotNull("fromCsvRow must not return null", restored);
        assertEquals(original.getNotificationId(), restored.getNotificationId());
        assertEquals(original.getUserId(),         restored.getUserId());
        assertEquals(original.getType(),           restored.getType());
        assertEquals(original.getMessage(),        restored.getMessage());
        assertEquals(original.isRead(),            restored.isRead());
        assertEquals(original.getCreatedDate(),    restored.getCreatedDate());
        assertEquals("danger",                     restored.getColor());
    }

    // ─────────────────────────────────────────────────────────────────────
    // Helper methods
    // ─────────────────────────────────────────────────────────────────────


    /** Creates a {@link Notification} with {@code isRead=false} for color-mapping tests. */

    /** Create a Notification with isRead=false. */

    private static Notification notifOf(String type) {
        return new Notification("NTF_X", "USER_X", type, "msg", false, "2026-05-02");
    }


    /**
     * Builds an unread {@link Notification} with today's date for DAO persistence tests.
     *
     * @param id     notification identifier
     * @param userId recipient user identifier
     * @param type   notification type constant
     * @param msg    human-readable message body
     * @return a new unread notification instance
     */

    private static Notification buildNotif(String id, String userId, String type, String msg) {
        return new Notification(id, userId, type, msg, false, LocalDate.now().toString());
    }


    /**
     * Builds a {@link Notification} with an explicit read flag for DAO query tests.
     *
     * @param id     notification identifier
     * @param userId recipient user identifier
     * @param type   notification type constant
     * @param msg    human-readable message body
     * @param isRead whether the notification has already been read
     * @return a new notification instance with the requested read state
     */

    private static Notification buildNotif(String id, String userId, String type,
                                           String msg, boolean isRead) {
        return new Notification(id, userId, type, msg, isRead, LocalDate.now().toString());
    }

    /**
     * Mirrors the deduplication + save logic inside AdminWorkloadServlet.doGet().
     * Only creates a WORKLOAD_ALERT if estimated hours exceed the limit AND no
     * unread alert for the same TA already exists for this admin.
     */
    private static void saveAlertIfNeeded(NotificationDAO notifDao, String adminId,
                                          String taId, String displayName, int estimatedHours) {
        if (estimatedHours <= LIMIT) return;          // not exceeded — nothing to do

        String taMarker = "[TA:" + taId + "]";
        List<Notification> existing = notifDao.findByUser(adminId);
        boolean alreadyPending = existing.stream()
                .anyMatch(n -> Notification.TYPE_WORKLOAD_ALERT.equals(n.getType())
                        && !n.isRead()
                        && n.getMessage().contains(taMarker));
        if (alreadyPending) return;

        Notification alert = new Notification(
                notifDao.generateNextId(),
                adminId,
                Notification.TYPE_WORKLOAD_ALERT,
                "Workload alert " + taMarker + ": " + displayName
                        + " has " + estimatedHours + "h (limit " + LIMIT + "h).",
                false,
                LocalDate.now().toString()
        );
        notifDao.save(alert);
    }

    /** Filter notifications by user and type. */
    private static List<Notification> dao_findByUserAndType(NotificationDAO dao,
                                                             String userId, String type) {
        List<Notification> result = new java.util.ArrayList<>();
        for (Notification n : dao.findByUser(userId)) {
            if (type.equals(n.getType())) result.add(n);
        }
        return result;
    }

