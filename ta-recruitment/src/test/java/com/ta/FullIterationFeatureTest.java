package com.ta;

import com.ta.dao.ApplicationDAO;
import com.ta.dao.JobDAO;
import com.ta.dao.UserDAO;
import com.ta.model.Application;
import com.ta.model.Job;
import com.ta.model.TAProfile;
import com.ta.model.User;
import com.ta.util.PasswordUtil;
import com.ta.util.Validator;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;

/**
 * 全量迭代测试脚本：Sprint 1 + Sprint 2 功能验证。
 * 使用方式：在 ta-recruitment 目录下运行 mvn -Dtest=FullIterationFeatureTest test
 */
public class FullIterationFeatureTest {

    private static final String DATA_DIR = "target/test-data";

    @BeforeClass
    public static void prepareTestDataDirectory() throws IOException {
        Path dataPath = Paths.get(DATA_DIR);
        if (Files.exists(dataPath)) {
            deleteRecursively(dataPath);
        }
        Files.createDirectories(dataPath);
    }

    @AfterClass
    public static void cleanUp() throws IOException {
        // 保留测试数据目录，方便验收时查看结果
    }

    // ── Sprint 1 ──────────────────────────────────────────────────────────

    @Test
    public void testRegistrationLoginRecovery() {
        User user = new User("U100", "student_x", PasswordUtil.hash("pass123"),
                "TA", "student_x@bupt.edu.cn", "pet", "dog", "ACTIVE");

        assertNotNull("Password hash should not be null", user.getPasswordHash());
        assertTrue("Correct password should be verified",
                PasswordUtil.verify("pass123", user.getPasswordHash()));
        assertFalse("Incorrect password should not pass verification",
                PasswordUtil.verify("wrongpass", user.getPasswordHash()));

        UserDAO userDao = new UserDAO(DATA_DIR);
        userDao.save(user);

        User loaded = userDao.findByUsername("student_x");
        assertNotNull("User should be searchable by username after saving", loaded);
        assertEquals("Saved user role should be TA", "TA", loaded.getRole());
        assertEquals("Saved user email should match", "student_x@bupt.edu.cn", loaded.getEmail());

        assertNull("Valid email should pass validation",
                Validator.validateEmail("valid@mail.com"));
        assertNotNull("Invalid email should return error",
                Validator.validateEmail("invalid-email"));
        assertNotNull("Too short password should return error",
                Validator.validatePassword("123"));
    }

    @Test
    public void testRolePermissionHandling() {
        User taUser    = new User("U101", "ta_user",    "hash", "TA",    "ta@bupt.edu.cn",    "q", "a", "ACTIVE");
        User moUser    = new User("U102", "mo_user",    "hash", "MO",    "mo@bupt.edu.cn",    "q", "a", "ACTIVE");
        User adminUser = new User("U103", "admin_user", "hash", "ADMIN", "admin@bupt.edu.cn", "q", "a", "ACTIVE");

        assertEquals("TA user should have TA role",       "TA",    taUser.getRole());
        assertEquals("MO user should have MO role",       "MO",    moUser.getRole());
        assertEquals("Admin user should have ADMIN role", "ADMIN", adminUser.getRole());
    }

    @Test
    public void testFormValidation() {
        assertNotNull("Empty username should return error",
                Validator.requireNonEmpty("", "Username"));
        assertNull("Non-empty username should pass",
                Validator.requireNonEmpty("student", "Username"));

        assertNotNull("Incorrect email format should return error",
                Validator.validateEmail("bad-format"));
        assertNull("Valid email should pass",
                Validator.validateEmail("test@bupt.edu.cn"));

        assertNotNull("Short password should return error",
                Validator.validatePassword("123"));
        assertNull("Password with at least 6 characters should pass",
                Validator.validatePassword("123456"));

        assertNotNull("Invalid phone number should return error",
                Validator.validatePhone("abc123"));
        assertNull("Valid phone number should pass",
                Validator.validatePhone("13812345678"));

        assertNotNull("Empty job module name should return error",
                Validator.validateJob("", "5", "2026-07-01"));
        assertNotNull("Non-integer vacancy count should return error",
                Validator.validateJob("AI", "zero", "2026-07-01"));
        assertNull("Valid job data should pass",
                Validator.validateJob("AI", "3", "2026-07-01"));
    }

    // ── Sprint 2 ──────────────────────────────────────────────────────────

    @Test
    public void testTaProfileAndCvUpload() {
        TAProfile profile = new TAProfile("U104", "2024210001", "Zhang San",
                "Software Engineering", "Year 3", "13811112222", "uploads/cv_zhangsan.pdf");

        assertEquals("Student ID should be saved correctly",
                "2024210001", profile.getStudentId());
        assertEquals("CV file path should be saved correctly",
                "uploads/cv_zhangsan.pdf", profile.getCvFilePath());

        assertNull("Complete profile data should pass validation",
                Validator.validateProfile(profile.getStudentId(), profile.getFullName(),
                        profile.getProgramme(), profile.getYearOfStudy()));
        assertNull("Valid CV file should pass validation",
                Validator.validateCvFile("cv.pdf", 1024 * 100));
        assertNotNull("Invalid CV format should return error",
                Validator.validateCvFile("cv.exe", 1024 * 100));
    }

    @Test
    public void testTaBrowseJobs() {
        JobDAO jobDao = new JobDAO(DATA_DIR);

        Job job1 = new Job("J100", "U102", "EBU6304", "Software Engineering",
                "TA Lab", "Assist lab sessions", "Java,SQL", 2,
                "2026-05-30", "Spring", "Grading and tutoring", "Year 3", "OPEN", "2026-04-05");
        Job job2 = new Job("J101", "U102", "EBU6304", "Software Engineering",
                "TA Grader", "Grade assignments", "Java", 1,
                "2026-06-01", "Spring", "Grading", "Year 2", "CLOSED", "2026-04-05");

        jobDao.save(job1);
        jobDao.save(job2);

        List<Job> openJobs = jobDao.findOpen();
        assertTrue("At least one open job should exist", openJobs.size() >= 1);
        assertTrue("Open job list should include J100",
                openJobs.stream().anyMatch(job -> "J100".equals(job.getJobId())));
    }

    @Test
    public void testTaApplyAndStatusFlow() {
        ApplicationDAO appDao = new ApplicationDAO(DATA_DIR);
        String nextAppId = appDao.generateNextId();

        Application application = new Application(nextAppId, "U100", "J100", "SUBMITTED", "2026-04-08", "");
        appDao.save(application);

        assertTrue("Should return true after TA has applied for a job",
                appDao.hasApplied("U100", "J100"));

        List<Application> taApps = appDao.findByTa("U100");
        assertTrue("TA should have at least one application record", taApps.size() >= 1);

        Application stored = taApps.get(0);
        stored.setStatus("UNDER_REVIEW");
        stored.setReviewNote("Waiting for MO feedback");
        appDao.update(stored);

        Application loaded = appDao.findById(stored.getApplicationId());
        assertEquals("Application status should be updated to UNDER_REVIEW",
                "UNDER_REVIEW", loaded.getStatus());
        assertEquals("Review notes should be saved",
                "Waiting for MO feedback", loaded.getReviewNote());
    }

    @Test
    public void testMoPostAndEditJob() {
        JobDAO jobDao = new JobDAO(DATA_DIR);
        String nextJobId = jobDao.generateNextId();

        Job job = new Job(nextJobId, "U102", "EBU6304", "Software Engineering",
                "TA Research Assistant", "Support research sessions", "Python,Data", 1,
                "2026-06-15", "Summer", "Lab assist", "Year 4", "OPEN", "2026-04-08");
        jobDao.save(job);

        Job saved = jobDao.findById(nextJobId);
        assertNotNull("Job should be searchable after saving", saved);
        assertEquals("Job title should be saved correctly",
                "TA Research Assistant", saved.getJobTitle());

        saved.setVacancies(2);
        saved.setDescription("Support research sessions and grading");
        jobDao.update(saved);

        Job updated = jobDao.findById(nextJobId);
        assertEquals("Vacancies should be updated after editing", 2, updated.getVacancies());
        assertEquals("Job description should be updated",
                "Support research sessions and grading", updated.getDescription());
    }

    @Test
    public void testMoReviewApplicantWorkflow() {
        ApplicationDAO appDao = new ApplicationDAO(DATA_DIR);
        List<Application> jobApps = appDao.findByJob("J100");
        assertTrue("MO should see at least one application when viewing the job applicant list",
                jobApps.size() >= 1);

        Application target = jobApps.get(0);
        target.setStatus("ACCEPTED");
        target.setReviewNote("Excellent fit");
        appDao.update(target);

        Application refreshed = appDao.findById(target.getApplicationId());
        assertEquals("Updated application status should be ACCEPTED",
                "ACCEPTED", refreshed.getStatus());
        assertEquals("Updated review notes should be saved",
                "Excellent fit", refreshed.getReviewNote());
    }

    @Test
    public void testHistoricalExportSimulation() throws IOException {
        Path exportPath = Paths.get(DATA_DIR, "archive", "history_export.csv");
        Files.createDirectories(exportPath.getParent());

        List<String> rows = List.of(
                User.CSV_HEADER,
                new User("U200", "export_test", PasswordUtil.hash("pass200"),
                        "TA", "export@bupt.edu.cn", "sq", "sa", "ACTIVE").toCsvRow());
        Files.write(exportPath, rows);

        List<String> readBack = Files.readAllLines(exportPath);
        assertEquals("Historical export file should have 2 lines", 2, readBack.size());
        assertEquals("First line of export file should be CSV header",
                User.CSV_HEADER, readBack.get(0));
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private static void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) return;
        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                });
    }
}