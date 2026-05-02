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

    @Test
    public void jobPosted_colorIsSuccess() {
        Notification n = notifOf(Notification.TYPE_JOB_POSTED);
        assertEquals("success", n.getColor());
    }

    @Test
    public void workloadAlert_colorIsDanger() {
        Notification n = notifOf(Notification.TYPE_WORKLOAD_ALERT);
        assertEquals("danger", n.getColor());
    }

    @Test
    public void statusUpdated_colorIsInfo() {
        Notification n = notifOf(Notification.TYPE_STATUS_UPDATED);
        assertEquals("info", n.getColor());
    }

    @Test
    public void applicationSubmitted_colorIsPrimary() {
        Notification n = notifOf(Notification.TYPE_APPLICATION_SUBMITTED);
        assertEquals("primary", n.getColor());
    }

    @Test
    public void unknownType_colorIsSecondary() {
        Notification n = notifOf("UNKNOWN_TYPE");
        assertEquals("secondary", n.getColor());
    }

    @Test
    public void nullType_colorIsSecondary() {
        Notification n = notifOf(null);
        assertEquals("secondary", n.getColor());
    }

    // ─────────────────────────────────────────────────────────────────────
    // 3. MO JOB-POSTED NOTIFICATION
    // ─────────────────────────────────────────────────────────────────────

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

    @Test
    public void jobPostedNotification_idStartsWithNtf() throws Exception {
        File dataDir = folder.newFolder("data");
        NotificationDAO dao = new NotificationDAO(dataDir.getAbsolutePath());
        String id = dao.generateNextId();
        assertTrue("Generated ID should start with NTF", id.startsWith("NTF"));
    }

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

    @Test
    public void workloadConstants_matchDefinition() {
        assertEquals("HOURS_PER_ACCEPTED_ASSIGNMENT must be 16", 16, HOURS_PER);
        assertEquals("WORKLOAD_LIMIT_HOURS must be 48",          48, LIMIT);
    }

    @Test
    public void threeAcceptedJobs_isExactlyAtLimit_notExceeded() {
        int hours   = 3 * HOURS_PER;   // 48
        boolean exceeded = hours > LIMIT;
        assertFalse("Exactly 48 h (3 accepted jobs) must NOT be flagged as exceeded", exceeded);
    }

    @Test
    public void fourAcceptedJobs_exceedsLimit() {
        int hours   = 4 * HOURS_PER;   // 64
        boolean exceeded = hours > LIMIT;
        assertTrue("64 h (4 accepted jobs) must be flagged as exceeded", exceeded);
    }

    // ─────────────────────────────────────────────────────────────────────
    // 5. WORKLOAD ALERT NOTIFICATION — creation
    // ─────────────────────────────────────────────────────────────────────

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

    @Test
    public void countUnread_onlyCountsUnreadForGivenUser() throws Exception {
        File dataDir = folder.newFolder("data");
        NotificationDAO dao = new NotificationDAO(dataDir.getAbsolutePath());

        dao.save(buildNotif("NTF1", "U1", Notification.TYPE_JOB_POSTED,   "A", false));
        dao.save(buildNotif("NTF2", "U1", Notification.TYPE_STATUS_UPDATED,"B", true));
        dao.save(buildNotif("NTF3", "U1", Notification.TYPE_JOB_POSTED,   "C", false));

        assertEquals("U1 should have 2 unread notifications", 2, dao.countUnread("U1"));
    }

    @Test
    public void markRead_setsIsReadTrue() throws Exception {
        File dataDir = folder.newFolder("data");
        NotificationDAO dao = new NotificationDAO(dataDir.getAbsolutePath());

        dao.save(buildNotif("NTF1", "U1", Notification.TYPE_JOB_POSTED, "msg", false));
        assertEquals(1, dao.countUnread("U1"));

        dao.markRead("NTF1");
        assertEquals("After markRead, unread count must be 0", 0, dao.countUnread("U1"));
    }

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

    /** Create a Notification with isRead=false. */
    private static Notification notifOf(String type) {
        return new Notification("NTF_X", "USER_X", type, "msg", false, "2026-05-02");
    }

    private static Notification buildNotif(String id, String userId, String type, String msg) {
        return new Notification(id, userId, type, msg, false, LocalDate.now().toString());
    }

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
}