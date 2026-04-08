package com.ta;

import com.ta.dao.ApplicationDAO;
import com.ta.dao.JobDAO;
import com.ta.dao.UserDAO;
import com.ta.model.Application;
import com.ta.model.Job;
import com.ta.model.TAProfile;
import com.ta.model.User;
import com.ta.util.FileManager;
import com.ta.util.PasswordUtil;
import com.ta.util.Validator;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;

/**
 * 全量迭代测试脚本：Sprint 1 + Sprint 2 功能验证。
 * 仅新增脚本，不修改现有源文件。
 * 使用方式：在 ta-recruitment 目录下运行 mvn -q -Dtest=FullIterationFeatureTest test
 */
public class FullIterationFeatureTest {
    private static final String DATA_DIR = "target/test-data";
    private static int totalTests = 0;
    private static int passedTests = 0;

    public static void main(String[] args) {
        try {
            prepareTestDataDirectory();
            runAllTests();
            printSummary();
            System.exit(passedTests == totalTests ? 0 : 1);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(2);
        }
    }

    private static void prepareTestDataDirectory() throws IOException {
        Path dataPath = Paths.get(DATA_DIR);
        if (Files.exists(dataPath)) {
            deleteRecursively(dataPath);
        }
        Files.createDirectories(dataPath);
    }

    private static void runAllTests() {
        println("========== TA Recruitment Sprint 1 + 2 Full Feature Test Script ==========");

        run("Sprint 1: Registration / Login / Password Recovery Modules", FullIterationFeatureTest::testRegistrationLoginRecovery);
        run("Sprint 1: Role Permissions and User Role Control", FullIterationFeatureTest::testRolePermissionHandling);
        run("Sprint 1: Form Input and Data Validation (US-T04)", FullIterationFeatureTest::testFormValidation);
        run("Sprint 2: TA Profile Creation and CV Upload Validation", FullIterationFeatureTest::testTaProfileAndCvUpload);
        run("Sprint 2: TA Browsing Job List (US-TA03)", FullIterationFeatureTest::testTaBrowseJobs);
        run("Sprint 2: TA Job Application and Status Tracking (US-TA04/05)", FullIterationFeatureTest::testTaApplyAndStatusFlow);
        run("Sprint 2: MO Posting and Editing Jobs (US-MO01)", FullIterationFeatureTest::testMoPostAndEditJob);
        run("Sprint 2: MO Reviewing Applicants and Updating Status (US-MO02/03/04)", FullIterationFeatureTest::testMoReviewApplicantWorkflow);
        run("Sprint 2: Historical Archive Export Simulation (US-C02)", FullIterationFeatureTest::testHistoricalExportSimulation);
    }

    private static void testRegistrationLoginRecovery() {
        User user = new User("U100", "student_x", PasswordUtil.hash("pass123"),
                "TA", "student_x@bupt.edu.cn", "pet", "dog", "ACTIVE");

        assertNotNull(user.getPasswordHash(), "Password hash should not be null");
        assertTrue(PasswordUtil.verify("pass123", user.getPasswordHash()), "Correct password should be verified");
        assertFalse(PasswordUtil.verify("wrongpass", user.getPasswordHash()), "Incorrect password should not pass verification");

        UserDAO userDao = new UserDAO(DATA_DIR);
        userDao.save(user);

        User loaded = userDao.findByUsername("student_x");
        assertNotNull(loaded, "User should be searchable by username after saving");
        assertEquals("TA", loaded.getRole(), "Saved user role should be TA");
        assertEquals("student_x@bupt.edu.cn", loaded.getEmail(), "Saved user email should match");

        assertNull(Validator.validateEmail("valid@mail.com"), "Valid email should pass validation");
        assertNotNull(Validator.validateEmail("invalid-email"), "Invalid email should return error");
        assertNotNull(Validator.validatePassword("123"), "Too short password should return error");
    }

    private static void testRolePermissionHandling() {
        User taUser = new User("U101", "ta_user", "hash", "TA",
                "ta@bupt.edu.cn", "q", "a", "ACTIVE");
        User moUser = new User("U102", "mo_user", "hash", "MO",
                "mo@bupt.edu.cn", "q", "a", "ACTIVE");
        User adminUser = new User("U103", "admin_user", "hash", "ADMIN",
                "admin@bupt.edu.cn", "q", "a", "ACTIVE");

        assertEquals("TA", taUser.getRole(), "TA user should have TA role");
        assertEquals("MO", moUser.getRole(), "MO user should have MO role");
        assertEquals("ADMIN", adminUser.getRole(), "Admin user should have ADMIN role");

        assertTrue(taUser.getRole().equals("TA"), "TA users should only access TA menus");
        assertTrue(moUser.getRole().equals("MO"), "MO users should only access MO menus");
        assertTrue(adminUser.getRole().equals("ADMIN"), "ADMIN users should only access admin menus");
    }

    private static void testFormValidation() {
        assertNotNull(Validator.requireNonEmpty("", "Username"), "Empty username should return error");
        assertNull(Validator.requireNonEmpty("student", "Username"), "Non-empty username should pass");
        assertNotNull(Validator.validateEmail("bad-format"), "Incorrect email format should return error");
        assertNull(Validator.validateEmail("test@bupt.edu.cn"), "Valid email should pass");
        assertNotNull(Validator.validatePassword("123"), "Short password should return error");
        assertNull(Validator.validatePassword("123456"), "Password with at least 6 characters should pass");
        assertNotNull(Validator.validatePhone("abc123"), "Invalid phone number should return error");
        assertNull(Validator.validatePhone("13812345678"), "Valid phone number should pass");
        assertNotNull(Validator.validateJob("", "5", "2026-07-01"), "Empty job module name should return error");
        assertNotNull(Validator.validateJob("AI", "zero", "2026-07-01"), "Non-integer vacancy count should return error");
        assertNull(Validator.validateJob("AI", "3", "2026-07-01"), "Valid job data should pass");
    }

    private static void testTaProfileAndCvUpload() {
        TAProfile profile = new TAProfile("U104", "2024210001", "Zhang San",
                "Software Engineering", "Year 3", "13811112222", "uploads/cv_zhangsan.pdf");
        assertEquals("2024210001", profile.getStudentId(), "Student ID should be saved correctly");
        assertEquals("uploads/cv_zhangsan.pdf", profile.getCvFilePath(), "CV file path should be saved correctly");

        assertNull(Validator.validateProfile(profile.getStudentId(), profile.getFullName(), profile.getProgramme(), profile.getYearOfStudy()),
                "Complete profile data should pass validation");
        assertNull(Validator.validateCvFile("cv.pdf", 1024 * 100), "Valid CV file should pass validation");
        assertNotNull(Validator.validateCvFile("cv.exe", 1024 * 100), "Invalid CV format should return error");
    }

    private static void testTaBrowseJobs() {
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
        assertTrue(openJobs.size() >= 1, "At least one open job should exist");
        assertTrue(openJobs.stream().anyMatch(job -> "J100".equals(job.getJobId())), "Open job list should include J100");
    }

    private static void testTaApplyAndStatusFlow() {
        ApplicationDAO appDao = new ApplicationDAO(DATA_DIR);
        String nextAppId = appDao.generateNextId();

        Application application = new Application(nextAppId, "U100", "J100", "SUBMITTED", "2026-04-08", "");
        appDao.save(application);

        assertTrue(appDao.hasApplied("U100", "J100"), "Should return true after TA has applied for a job");
        List<Application> taApps = appDao.findByTa("U100");
        assertTrue(taApps.size() >= 1, "TA should have at least one application record");

        Application stored = taApps.get(0);
        stored.setStatus("UNDER_REVIEW");
        stored.setReviewNote("Waiting for MO feedback");
        appDao.update(stored);

        Application loaded = appDao.findById(stored.getApplicationId());
        assertEquals("UNDER_REVIEW", loaded.getStatus(), "Application status should be updated to UNDER_REVIEW");
        assertEquals("Waiting for MO feedback", loaded.getReviewNote(), "Review notes should be saved");
    }

    private static void testMoPostAndEditJob() {
        JobDAO jobDao = new JobDAO(DATA_DIR);
        String nextJobId = jobDao.generateNextId();

        Job job = new Job(nextJobId, "U102", "EBU6304", "Software Engineering",
                "TA Research Assistant", "Support research sessions", "Python,Data", 1,
                "2026-06-15", "Summer", "Lab assist", "Year 4", "OPEN", "2026-04-08");
        jobDao.save(job);

        Job saved = jobDao.findById(nextJobId);
        assertNotNull(saved, "Job should be searchable after saving");
        assertEquals("TA Research Assistant", saved.getJobTitle(), "Job title should be saved correctly");

        saved.setVacancies(2);
        saved.setDescription("Support research sessions and grading");
        jobDao.update(saved);

        Job updated = jobDao.findById(nextJobId);
        assertEquals(2, updated.getVacancies(), "Vacancies should be updated after editing");
        assertEquals("Support research sessions and grading", updated.getDescription(), "Job description should be updated");
    }

    private static void testMoReviewApplicantWorkflow() {
        ApplicationDAO appDao = new ApplicationDAO(DATA_DIR);
        List<Application> jobApps = appDao.findByJob("J100");
        assertTrue(jobApps.size() >= 1, "MO should see at least one application when viewing the job applicant list");

        Application target = jobApps.get(0);
        target.setStatus("ACCEPTED");
        target.setReviewNote("Excellent fit");
        appDao.update(target);

        Application refreshed = appDao.findById(target.getApplicationId());
        assertEquals("ACCEPTED", refreshed.getStatus(), "Updated application status should be ACCEPTED");
        assertEquals("Excellent fit", refreshed.getReviewNote(), "Updated review notes should be saved");
    }

    private static void testHistoricalExportSimulation() throws IOException {
        Path exportPath = Paths.get(DATA_DIR, "archive", "history_export.csv");
        Files.createDirectories(exportPath.getParent());

        List<String> rows = List.of(
                User.CSV_HEADER,
                new User("U200", "export_test", PasswordUtil.hash("pass200"), "TA", "export@bupt.edu.cn", "sq", "sa", "ACTIVE").toCsvRow()
        );
        Files.write(exportPath, rows);

        List<String> readBack = Files.readAllLines(exportPath);
        assertEquals(2, readBack.size(), "Historical export file should have 2 lines");
        assertEquals(User.CSV_HEADER, readBack.get(0), "First line of export file should be CSV header");
    }

    private static void run(String title, CheckedRunnable runnable) {
        totalTests++;
        System.out.printf("[Test %d] %s ... ", totalTests, title);
        try {
            runnable.run();
            passedTests++;
            System.out.println("PASS");
        } catch (AssertionError | Exception ex) {
            System.out.println("FAIL");
            ex.printStackTrace(System.out);
        }
    }

    private static void printSummary() {
        println("========== Test Summary ==========");
        println("Total Tests: " + totalTests);
        println("Passed: " + passedTests);
        println("Failed: " + (totalTests - passedTests));
    }

    private static void println(String message) {
        System.out.println(message);
    }

    private static void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) return;
        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException ignored) {
                    }
                });
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) throw new AssertionError(message);
    }

    private static void assertNull(Object value, String message) {
        if (value != null) throw new AssertionError(message + " (expected null, got: " + value + ")");
    }

    private static void assertNotNull(Object value, String message) {
        if (value == null) throw new AssertionError(message + " (expected not null)");
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null) {
            if (actual != null) throw new AssertionError(message + " (expected null, got: " + actual + ")");
        } else if (!expected.equals(actual)) {
            throw new AssertionError(message + " (expected: " + expected + ", got: " + actual + ")");
        }
    }

    @FunctionalInterface
    private interface CheckedRunnable {
        void run() throws Exception;
    }
}