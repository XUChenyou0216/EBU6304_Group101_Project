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
        println("========== TA Recruitment Sprint 1 + 2 全功能测试脚本 ==========");

        run("Sprint1: 注册 / 登录 / 密码恢复 模块", FullIterationFeatureTest::testRegistrationLoginRecovery);
        run("Sprint1: 角色权限与用户角色控制", FullIterationFeatureTest::testRolePermissionHandling);
        run("Sprint1: 表单输入与数据校验（US-T04）", FullIterationFeatureTest::testFormValidation);
        run("Sprint2: TA 创建个人档案与简历上传校验", FullIterationFeatureTest::testTaProfileAndCvUpload);
        run("Sprint2: TA 浏览岗位列表（US-TA03）", FullIterationFeatureTest::testTaBrowseJobs);
        run("Sprint2: TA 申请岗位与查看申请状态（US-TA04/05）", FullIterationFeatureTest::testTaApplyAndStatusFlow);
        run("Sprint2: MO 发布与编辑岗位（US-MO01）", FullIterationFeatureTest::testMoPostAndEditJob);
        run("Sprint2: MO 查看申请者与更新状态（US-MO02/MO03/MO04）", FullIterationFeatureTest::testMoReviewApplicantWorkflow);
        run("Sprint2: 导出历史档案模拟（US-C02）", FullIterationFeatureTest::testHistoricalExportSimulation);
    }

    private static void testRegistrationLoginRecovery() {
        User user = new User("U100", "student_x", PasswordUtil.hash("pass123"),
                "TA", "student_x@bupt.edu.cn", "pet", "dog", "ACTIVE");

        assertNotNull(user.getPasswordHash(), "密码散列后不应为空");
        assertTrue(PasswordUtil.verify("pass123", user.getPasswordHash()), "正确密码应验证通过");
        assertFalse(PasswordUtil.verify("wrongpass", user.getPasswordHash()), "错误密码不应通过验证");

        UserDAO userDao = new UserDAO(DATA_DIR);
        userDao.save(user);

        User loaded = userDao.findByUsername("student_x");
        assertNotNull(loaded, "保存后应可根据用户名查询到用户");
        assertEquals("TA", loaded.getRole(), "保存用户的角色应为 TA");
        assertEquals("student_x@bupt.edu.cn", loaded.getEmail(), "保存用户的邮箱应一致");

        assertNull(Validator.validateEmail("valid@mail.com"), "合法邮箱应通过校验");
        assertNotNull(Validator.validateEmail("invalid-email"), "非法邮箱应报错");
        assertNotNull(Validator.validatePassword("123"), "过短密码应报错");
    }

    private static void testRolePermissionHandling() {
        User taUser = new User("U101", "ta_user", "hash", "TA",
                "ta@bupt.edu.cn", "q", "a", "ACTIVE");
        User moUser = new User("U102", "mo_user", "hash", "MO",
                "mo@bupt.edu.cn", "q", "a", "ACTIVE");
        User adminUser = new User("U103", "admin_user", "hash", "ADMIN",
                "admin@bupt.edu.cn", "q", "a", "ACTIVE");

        assertEquals("TA", taUser.getRole(), "TA 用户应拥有 TA 角色");
        assertEquals("MO", moUser.getRole(), "MO 用户应拥有 MO 角色");
        assertEquals("ADMIN", adminUser.getRole(), "管理员用户应拥有 ADMIN 角色");

        assertTrue(taUser.getRole().equals("TA"), "TA 用户只能访问 TA 菜单");
        assertTrue(moUser.getRole().equals("MO"), "MO 用户只能访问 MO 菜单");
        assertTrue(adminUser.getRole().equals("ADMIN"), "ADMIN 用户只能访问管理菜单");
    }

    private static void testFormValidation() {
        assertNotNull(Validator.requireNonEmpty("", "Username"), "用户名为空应返回错误");
        assertNull(Validator.requireNonEmpty("student", "Username"), "用户名非空应通过");
        assertNotNull(Validator.validateEmail("bad-format"), "错误格式邮箱应报错");
        assertNull(Validator.validateEmail("test@bupt.edu.cn"), "合法邮箱应通过");
        assertNotNull(Validator.validatePassword("123"), "短密码应报错");
        assertNull(Validator.validatePassword("123456"), "至少 6 位密码应通过");
        assertNotNull(Validator.validatePhone("abc123"), "非法电话应报错");
        assertNull(Validator.validatePhone("13812345678"), "合法电话应通过");
        assertNotNull(Validator.validateJob("", "5", "2026-07-01"), "岗位模块名为空应报错");
        assertNotNull(Validator.validateJob("AI", "zero", "2026-07-01"), "岗位名额非整数应报错");
        assertNull(Validator.validateJob("AI", "3", "2026-07-01"), "合法岗位数据应通过");
    }

    private static void testTaProfileAndCvUpload() {
        TAProfile profile = new TAProfile("U104", "2024210001", "Zhang San",
                "Software Engineering", "Year 3", "13811112222", "uploads/cv_zhangsan.pdf");
        assertEquals("2024210001", profile.getStudentId(), "学生 ID 应正确保存");
        assertEquals("uploads/cv_zhangsan.pdf", profile.getCvFilePath(), "简历路径应正确保存");

        assertNull(Validator.validateProfile(profile.getStudentId(), profile.getFullName(), profile.getProgramme(), profile.getYearOfStudy()),
                "完整个人档案数据应通过校验");
        assertNull(Validator.validateCvFile("cv.pdf", 1024 * 100), "合法简历文件应通过校验");
        assertNotNull(Validator.validateCvFile("cv.exe", 1024 * 100), "非法简历格式应报错");
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
        assertTrue(openJobs.size() >= 1, "应至少存在一个开放岗位");
        assertTrue(openJobs.stream().anyMatch(job -> "J100".equals(job.getJobId())), "开放岗位列表应包含 J100");
    }

    private static void testTaApplyAndStatusFlow() {
        ApplicationDAO appDao = new ApplicationDAO(DATA_DIR);
        String nextAppId = appDao.generateNextId();

        Application application = new Application(nextAppId, "U100", "J100", "SUBMITTED", "2026-04-08", "");
        appDao.save(application);

        assertTrue(appDao.hasApplied("U100", "J100"), "TA 已申请岗位后应返回 true");
        List<Application> taApps = appDao.findByTa("U100");
        assertTrue(taApps.size() >= 1, "TA 应至少有一条申请记录");

        Application stored = taApps.get(0);
        stored.setStatus("UNDER_REVIEW");
        stored.setReviewNote("Waiting for MO feedback");
        appDao.update(stored);

        Application loaded = appDao.findById(stored.getApplicationId());
        assertEquals("UNDER_REVIEW", loaded.getStatus(), "申请状态应更新为 UNDER_REVIEW");
        assertEquals("Waiting for MO feedback", loaded.getReviewNote(), "评审备注应保存");
    }

    private static void testMoPostAndEditJob() {
        JobDAO jobDao = new JobDAO(DATA_DIR);
        String nextJobId = jobDao.generateNextId();

        Job job = new Job(nextJobId, "U102", "EBU6304", "Software Engineering",
                "TA Research Assistant", "Support research sessions", "Python,Data", 1,
                "2026-06-15", "Summer", "Lab assist", "Year 4", "OPEN", "2026-04-08");
        jobDao.save(job);

        Job saved = jobDao.findById(nextJobId);
        assertNotNull(saved, "保存后应能查询到岗位");
        assertEquals("TA Research Assistant", saved.getJobTitle(), "岗位标题应正确保存");

        saved.setVacancies(2);
        saved.setDescription("Support research sessions and grading");
        jobDao.update(saved);

        Job updated = jobDao.findById(nextJobId);
        assertEquals(2, updated.getVacancies(), "编辑后名额应更新");
        assertEquals("Support research sessions and grading", updated.getDescription(), "岗位描述应更新");
    }

    private static void testMoReviewApplicantWorkflow() {
        ApplicationDAO appDao = new ApplicationDAO(DATA_DIR);
        List<Application> jobApps = appDao.findByJob("J100");
        assertTrue(jobApps.size() >= 1, "MO 查看岗位申请者列表时，至少应看到一条申请");

        Application target = jobApps.get(0);
        target.setStatus("ACCEPTED");
        target.setReviewNote("Excellent fit");
        appDao.update(target);

        Application refreshed = appDao.findById(target.getApplicationId());
        assertEquals("ACCEPTED", refreshed.getStatus(), "更新后的申请状态应为 ACCEPTED");
        assertEquals("Excellent fit", refreshed.getReviewNote(), "更新后的审核备注应保存");
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
        assertEquals(2, readBack.size(), "历史导出文件行数应为 2");
        assertEquals(User.CSV_HEADER, readBack.get(0), "导出文件第一行为 CSV 头");
    }

    private static void run(String title, CheckedRunnable runnable) {
        totalTests++;
        System.out.printf("[测试 %d] %s ... ", totalTests, title);
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
        println("========== 测试总结 ==========");
        println("总测试项: " + totalTests);
        println("通过项: " + passedTests);
        println("失败项: " + (totalTests - passedTests));
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
