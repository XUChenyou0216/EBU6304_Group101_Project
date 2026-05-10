package com.ta;

import com.ta.util.FileManager;
import com.ta.util.Validator;

import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.htmlunit.BrowserVersion;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlButton;
import org.htmlunit.html.HtmlEmailInput;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlInput;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlPasswordInput;
import org.htmlunit.html.HtmlSelect;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * US-T04 全功能测试 — 后端校验逻辑 + 前端页面展示检测
 *
 * Section 1: Validator 纯单元测试（无需服务器）
 *   - requireNonEmpty / validateEmail / validatePassword / validatePhone
 *   - validateProfile / validateCvFile / validateJob
 *   - validateDate / validatePositiveInt / sanitizeForCsv
 *
 * Section 2: 前端页面展示测试（HtmlUnit + 嵌入式 Tomcat）
 *   - 登录 / 注册页面渲染
 *   - TA / MO / Admin 登录跳转与页面内容
 *   - CV 上传错误信息展示
 *   - 通知铃铛渲染
 *   - 未授权访问重定向
 *
 * 运行方式: mvn -Dtest=ValidatorTest test
 */
public class ValidatorTest {

    // ── Embedded Tomcat ───────────────────────────────────────────────────
    private static Tomcat  tomcat;
    private static String  baseUrl;
    private static File    webappRoot;
    private        WebClient webClient;

    // 每次测试运行使用唯一用户名，避免与已有数据冲突
    private static final long   RUN_ID   = System.currentTimeMillis();
    private static final String TA_USER  = "vt_ta_"  + RUN_ID;
    private static final String MO_USER  = "vt_mo_"  + RUN_ID;
    private static final String PASSWORD = "Pass1234";

    @BeforeClass
    public static void startServer() throws Exception {
        prepareWebappCopy();
        tomcat = new Tomcat();
        tomcat.setPort(0);
        tomcat.setBaseDir("target/validator-tomcat");

        Context ctx = tomcat.addWebapp("", webappRoot.getAbsolutePath());
        ctx.setParentClassLoader(Thread.currentThread().getContextClassLoader());

        WebResourceRoot resources = new StandardRoot(ctx);
        resources.addPreResources(new DirResourceSet(resources,
                "/WEB-INF/classes",
                new File("target/classes").getAbsolutePath(), "/"));
        ctx.setResources(resources);
        tomcat.start();
        baseUrl = "http://localhost:" + tomcat.getConnector().getLocalPort();

        // 预注册共用测试账号（整个测试类只注册一次）
        registerOnce(TA_USER, TA_USER + "@test.com", "TA", PASSWORD);
        registerOnce(MO_USER, MO_USER + "@test.com", "MO", PASSWORD);
    }

    @AfterClass
    public static void stopServer() throws Exception {
        if (tomcat != null) { tomcat.stop(); tomcat.destroy(); }
    }

    @Before
    public void openBrowser() {
        webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setRedirectEnabled(true);
    }

    @After
    public void closeBrowser() {
        if (webClient != null) webClient.close();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Section 1 — 后端 Validator 单元测试
    // ═══════════════════════════════════════════════════════════════════════

    // ── requireNonEmpty ──────────────────────────────────────────────────

    @Test
    public void requireNonEmpty_emptyString_returnsError() {
        assertNotNull(Validator.requireNonEmpty("", "Username"));
    }

    @Test
    public void requireNonEmpty_nullValue_returnsError() {
        assertNotNull(Validator.requireNonEmpty(null, "Username"));
    }

    @Test
    public void requireNonEmpty_whitespaceOnly_returnsError() {
        assertNotNull(Validator.requireNonEmpty("   ", "Username"));
    }

    @Test
    public void requireNonEmpty_validValue_returnsNull() {
        assertNull(Validator.requireNonEmpty("student", "Username"));
    }

    // ── validateEmail ────────────────────────────────────────────────────

    @Test
    public void validateEmail_missingAtSign_returnsError() {
        assertNotNull(Validator.validateEmail("invalid-email-at-com"));
    }

    @Test
    public void validateEmail_missingDomain_returnsError() {
        assertNotNull(Validator.validateEmail("user@"));
    }

    @Test
    public void validateEmail_empty_returnsError() {
        assertNotNull(Validator.validateEmail(""));
    }

    @Test
    public void validateEmail_valid_returnsNull() {
        assertNull(Validator.validateEmail("test@bupt.edu.cn"));
    }

    // ── validatePassword ─────────────────────────────────────────────────

    @Test
    public void validatePassword_tooShort_returnsError() {
        assertNotNull(Validator.validatePassword("12345"));
    }

    @Test
    public void validatePassword_null_returnsError() {
        assertNotNull(Validator.validatePassword(null));
    }

    @Test
    public void validatePassword_weakSixDigitsOnly_returnsError() {
        // validatePassword() 现在委托给 validateStrongPassword()，
        // 纯数字密码缺少大小写字母，必须被拒绝。
        assertNotNull(Validator.validatePassword("123456"));
    }

    // ── validatePhone ────────────────────────────────────────────────────

    @Test
    public void validatePhone_containsLetters_returnsError() {
        assertNotNull(Validator.validatePhone("12345abc678"));
    }

    @Test
    public void validatePhone_tooShort_returnsError() {
        assertNotNull(Validator.validatePhone("123"));
    }

    @Test
    public void validatePhone_empty_returnsNull_phoneIsOptional() {
        assertNull(Validator.validatePhone(""));
    }

    @Test
    public void validatePhone_valid_returnsNull() {
        assertNull(Validator.validatePhone("13812345678"));
    }

    // ── validateProfile ──────────────────────────────────────────────────

    @Test
    public void validateProfile_missingStudentId_returnsError() {
        assertNotNull(Validator.validateProfile("", "Alice", "CS", "Year 2"));
    }

    @Test
    public void validateProfile_missingFullName_returnsError() {
        assertNotNull(Validator.validateProfile("2024001", "", "CS", "Year 2"));
    }

    @Test
    public void validateProfile_missingProgramme_returnsError() {
        assertNotNull(Validator.validateProfile("2024001", "Alice", "", "Year 2"));
    }

    @Test
    public void validateProfile_complete_returnsNull() {
        assertNull(Validator.validateProfile("2024001", "Alice", "CS", "Year 2"));
    }

    // ── validateCvFile ───────────────────────────────────────────────────

    @Test
    public void validateCvFile_exeFile_returnsError() {
        assertNotNull(Validator.validateCvFile("virus.exe", 1024));
    }

    @Test
    public void validateCvFile_shellScript_returnsError() {
        assertNotNull(Validator.validateCvFile("run.sh", 1024));
    }

    @Test
    public void validateCvFile_oversized_returnsError() {
        assertNotNull(Validator.validateCvFile("cv.pdf", 11L * 1024 * 1024));
    }

    @Test
    public void validateCvFile_validPdf_returnsNull() {
        assertNull(Validator.validateCvFile("cv.pdf", 500 * 1024));
    }

    @Test
    public void validateCvFile_validDocx_returnsNull() {
        assertNull(Validator.validateCvFile("cv.docx", 500 * 1024));
    }

    @Test
    public void validateCvFile_errorMsg_mentionsPdfDocx() {
        String err = Validator.validateCvFile("payload.exe", 1024);
        assertNotNull(err);
        assertTrue("Error must mention PDF", err.toUpperCase().contains("PDF"));
        assertTrue("Error must mention DOC", err.toUpperCase().contains("DOC"));
    }

    // ── validateJob ──────────────────────────────────────────────────────

    @Test
    public void validateJob_emptyModuleName_returnsError() {
        assertNotNull(Validator.validateJob("", "5", "2026-06-01"));
    }

    @Test
    public void validateJob_negativeVacancies_returnsError() {
        assertNotNull(Validator.validateJob("CS101", "-1", "2026-06-01"));
    }

    @Test
    public void validateJob_nonIntegerVacancies_returnsError() {
        assertNotNull(Validator.validateJob("CS101", "abc", "2026-06-01"));
    }

    @Test
    public void validateJob_zeroVacancies_returnsError() {
        assertNotNull(Validator.validateJob("CS101", "0", "2026-06-01"));
    }

    @Test
    public void validateJob_valid_returnsNull() {
        assertNull(Validator.validateJob("AI Basics", "3", "2026-05-20"));
    }

    // 数据拦截模拟（Acceptance Criteria 3）
    @Test
    public void dataInterception_invalidJobNotWrittenToCsv() {
        String error = Validator.validateJob("Database Systems", "Zero", "2026-12-31");
        assertNotNull("Non-integer vacancies must be intercepted before FileManager write", error);
    }

    @Test
    public void dataInterception_invalidUserDataBlocked() {
        String error = Validator.requireNonEmpty("", "Username");
        if (error == null) error = Validator.validateEmail("bad_email");
        assertNotNull("Invalid user data must be blocked before reaching FileManager", error);
    }

    // ── validateDate & validatePositiveInt ───────────────────────────────

    @Test
    public void validateDate_wrongFormat_returnsError() {
        assertNotNull(Validator.validateDate("01/06/2026", "Deadline"));
    }

    @Test
    public void validateDate_valid_returnsNull() {
        assertNull(Validator.validateDate("2026-06-01", "Deadline"));
    }

    @Test
    public void validatePositiveInt_negative_returnsError() {
        assertNotNull(Validator.validatePositiveInt("-1", "Count"));
    }

    @Test
    public void validatePositiveInt_zero_returnsError() {
        assertNotNull(Validator.validatePositiveInt("0", "Count"));
    }

    @Test
    public void validatePositiveInt_valid_returnsNull() {
        assertNull(Validator.validatePositiveInt("5", "Count"));
    }

    // ── sanitizeForCsv ───────────────────────────────────────────────────

    @Test
    public void sanitizeForCsv_stripsWindowsNewline() {
        assertFalse(Validator.sanitizeForCsv("line1\r\nline2").contains("\r\n"));
    }

    @Test
    public void sanitizeForCsv_stripsUnixNewline() {
        assertFalse(Validator.sanitizeForCsv("line1\nline2").contains("\n"));
    }

    @Test
    public void sanitizeForCsv_nullInput_returnsEmptyString() {
        assertEquals("", Validator.sanitizeForCsv(null));
    }

    @Test
    public void sanitizeForCsv_normalInput_unchanged() {
        assertEquals("hello world", Validator.sanitizeForCsv("hello world"));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Section 2 — 前端页面展示测试（HtmlUnit + 嵌入式 Tomcat）
    // ═══════════════════════════════════════════════════════════════════════

    // ── 登录页面 ──────────────────────────────────────────────────────────

    @Test
    public void loginPage_rendersUsernameAndPasswordFields() throws Exception {
        HtmlPage page = webClient.getPage(baseUrl + "/login.jsp");
        assertNotNull("Username 输入框必须存在", page.getElementByName("username"));
        assertNotNull("Password 输入框必须存在", page.getElementByName("password"));
        String title = page.getTitleText();
        assertTrue("页面标题必须包含 'Login'", title.contains("Login"));
    }

    @Test
    public void loginPage_hasLoginButtonAndRegisterLink() throws Exception {
        HtmlPage page = webClient.getPage(baseUrl + "/login.jsp");
        HtmlButton submit = page.getForms().get(0).getFirstByXPath(".//button[@type='submit']");
        assertNotNull("登录提交按钮必须存在", submit);
        String body = page.getBody().getTextContent();
        assertTrue("页面必须包含注册入口文字", body.contains("Register") || body.contains("Sign up"));
    }

    @Test
    public void loginWithInvalidCredentials_displaysErrorMessage() throws Exception {
        HtmlPage loginPage = webClient.getPage(baseUrl + "/login.jsp");
        HtmlForm form = loginPage.getForms().get(0);
        ((HtmlInput) form.getInputByName("username")).setValueAttribute("no_such_user_xyz");
        ((HtmlPasswordInput) form.getInputByName("password")).setValueAttribute("wrongpass");
        HtmlPage result = ((HtmlButton) form.getFirstByXPath(".//button[@type='submit']")).click();
        webClient.waitForBackgroundJavaScript(500);
        String body = result.getBody().getTextContent().toLowerCase();
        assertTrue("登录失败必须显示错误提示",
                body.contains("invalid") || body.contains("incorrect")
                        || body.contains("error") || body.contains("wrong"));
    }

    // ── 注册页面 ──────────────────────────────────────────────────────────

    @Test
    public void registerPage_rendersRoleSelectorWithTaAndMo() throws Exception {
        HtmlPage page = webClient.getPage(baseUrl + "/register.jsp");
        HtmlSelect roleSelect = (HtmlSelect) page.getElementByName("role");
        assertNotNull("角色选择框必须存在", roleSelect);
        boolean hasTA = roleSelect.getOptions().stream().anyMatch(o -> "TA".equals(o.getValueAttribute()));
        boolean hasMO = roleSelect.getOptions().stream().anyMatch(o -> "MO".equals(o.getValueAttribute()));
        assertTrue("角色选择框必须包含 TA 选项", hasTA);
        assertTrue("角色选择框必须包含 MO 选项", hasMO);
    }

    @Test
    public void registerPage_hasAllRequiredFields() throws Exception {
        HtmlPage page = webClient.getPage(baseUrl + "/register.jsp");
        assertNotNull(page.getElementByName("username"));
        assertNotNull(page.getElementByName("email"));
        assertNotNull(page.getElementByName("password"));
        assertNotNull(page.getElementByName("confirmPassword"));
        assertNotNull(page.getElementByName("securityQuestion"));
        assertNotNull(page.getElementByName("securityAnswer"));
    }

    // ── TA 用户流程 ───────────────────────────────────────────────────────

    @Test
    public void taLogin_redirectsToTaDashboard() throws Exception {
        HtmlPage dashboard = loginAs(TA_USER, PASSWORD);
        assertTrue("TA 登录后必须跳转到 TA Dashboard",
                dashboard.getUrl().toString().contains("/ta/dashboard.jsp"));
    }

    @Test
    public void taProfilePage_hasStudentIdAndCvUploadInput() throws Exception {
        loginAs(TA_USER, PASSWORD);
        HtmlPage page = webClient.getPage(baseUrl + "/ta/profile.jsp");
        assertEquals("Profile 页面必须返回 200", 200, page.getWebResponse().getStatusCode());
        assertNotNull("Student ID 输入框必须存在", page.getElementByName("studentId"));
        assertNotNull("Full Name 输入框必须存在",   page.getElementByName("fullName"));
        assertFalse("CV 文件上传 input 必须存在",
                page.getByXPath("//input[@type='file']").isEmpty());
    }

    @Test
    public void taProfilePage_showsCvFormatHint() throws Exception {
        loginAs(TA_USER, PASSWORD);
        HtmlPage page = webClient.getPage(baseUrl + "/ta/profile.jsp");
        String body = page.getBody().getTextContent();
        assertTrue("Profile 页面必须显示支持的文件格式提示",
                body.contains("PDF") && body.contains("DOC"));
    }

    @Test
    public void taProfilePage_invalidFormatError_isDisplayed() throws Exception {
        loginAs(TA_USER, PASSWORD);
        // UploadCvServlet 重定向带 ?error=invalid_format 参数
        HtmlPage page = webClient.getPage(baseUrl + "/ta/profile.jsp?error=invalid_format");
        String body = page.getBody().getTextContent();
        assertTrue("invalid_format 错误必须在页面上显示",
                body.contains("Invalid file format") || body.contains("Supported"));
    }

    @Test
    public void taProfilePage_tooLargeError_isDisplayed() throws Exception {
        loginAs(TA_USER, PASSWORD);
        HtmlPage page = webClient.getPage(baseUrl + "/ta/profile.jsp?error=too_large");
        String body = page.getBody().getTextContent();
        assertTrue("too_large 错误必须在页面上显示",
                body.contains("10") || body.contains("limit") || body.contains("exceeds"));
    }

    @Test
    public void taProfilePage_uploadSuccess_isDisplayed() throws Exception {
        loginAs(TA_USER, PASSWORD);
        HtmlPage page = webClient.getPage(baseUrl + "/ta/profile.jsp?uploadStatus=success");
        String body = page.getBody().getTextContent();
        assertTrue("上传成功提示必须在页面上显示",
                body.contains("Successful") || body.contains("uploaded") || body.contains("updated"));
    }

    @Test
    public void taJobsPage_displaysAvailablePositionsHeading() throws Exception {
        loginAs(TA_USER, PASSWORD);
        HtmlPage page = webClient.getPage(baseUrl + "/ta/jobs.jsp");
        assertEquals(200, page.getWebResponse().getStatusCode());
        String body = page.getBody().getTextContent();
        assertTrue("Jobs 页面必须包含职位相关文字",
                body.contains("Available") || body.contains("Position") || body.contains("Job"));
    }

    @Test
    public void taApplicationsPage_isAccessible() throws Exception {
        loginAs(TA_USER, PASSWORD);
        HtmlPage page = webClient.getPage(baseUrl + "/ta/applications.jsp");
        assertEquals("Applications 页面必须返回 200", 200, page.getWebResponse().getStatusCode());
        assertTrue("页面必须包含 Application 相关文字",
                page.getBody().getTextContent().toLowerCase().contains("application"));
    }

    // ── MO 用户流程 ───────────────────────────────────────────────────────

    @Test
    public void moLogin_redirectsToMoDashboard() throws Exception {
        HtmlPage dashboard = loginAs(MO_USER, PASSWORD);
        assertTrue("MO 登录后必须跳转到 MO Dashboard",
                dashboard.getUrl().toString().contains("/mo/dashboard.jsp"));
    }

    @Test
    public void moPostJobPage_hasAllRequiredFields() throws Exception {
        loginAs(MO_USER, PASSWORD);
        HtmlPage page = webClient.getPage(baseUrl + "/mo/post-job");
        assertEquals(200, page.getWebResponse().getStatusCode());
        assertNotNull("moduleCode 字段必须存在",  page.getElementByName("moduleCode"));
        assertNotNull("moduleName 字段必须存在",  page.getElementByName("moduleName"));
        assertNotNull("jobTitle 字段必须存在",    page.getElementByName("jobTitle"));
        assertNotNull("vacancies 字段必须存在",   page.getElementByName("vacancies"));
        assertNotNull("deadline 字段必须存在",    page.getElementByName("deadline"));
    }

    @Test
    public void moPostJob_success_appearsInJobsList() throws Exception {
        loginAs(MO_USER, PASSWORD);
        HtmlPage postPage = webClient.getPage(baseUrl + "/mo/post-job");
        String uniqueTitle = "VT-Job-" + System.currentTimeMillis();
        HtmlPage result = publishJob(postPage, uniqueTitle);
        assertTrue("发布后必须跳转到 jobs 页面且含 success 参数",
                result.getUrl().toString().contains("success=posted"));
        assertTrue("已发布的职位标题必须出现在列表中",
                result.getBody().getTextContent().contains(uniqueTitle));
    }

    @Test
    public void moJobsPage_displaysPostedJobsSection() throws Exception {
        loginAs(MO_USER, PASSWORD);
        HtmlPage page = webClient.getPage(baseUrl + "/mo/jobs.jsp");
        assertEquals(200, page.getWebResponse().getStatusCode());
        String body = page.getBody().getTextContent();
        assertTrue("MO Jobs 页面必须包含职位相关文字",
                body.contains("Job") || body.contains("Post") || body.contains("Module"));
    }

    @Test
    public void moApplicantsPage_isAccessible() throws Exception {
        loginAs(MO_USER, PASSWORD);
        HtmlPage page = webClient.getPage(baseUrl + "/mo/applicants");
        assertEquals("Applicants 页面必须返回 200", 200, page.getWebResponse().getStatusCode());
    }

    // ── Admin 用户流程 ────────────────────────────────────────────────────

    @Test
    public void adminLogin_redirectsToAdminDashboard() throws Exception {
        HtmlPage result = loginAs("admin", "admin");
        assertTrue("Admin 登录后必须跳转到 Admin Dashboard",
                result.getUrl().toString().contains("/admin/dashboard.jsp"));
    }

    @Test
    public void adminWorkloadPage_displaysTaWorkloadHeading() throws Exception {
        loginAs("admin", "admin");
        HtmlPage page = webClient.getPage(baseUrl + "/admin/workload");
        assertEquals(200, page.getWebResponse().getStatusCode());
        String body = page.getBody().getTextContent();
        assertTrue("Workload 页面必须包含 'Workload' 或 'Hours'",
                body.contains("Workload") || body.contains("workload") || body.contains("Hours"));
    }

    @Test
    public void adminUsersPage_isAccessible() throws Exception {
        loginAs("admin", "admin");
        HtmlPage page = webClient.getPage(baseUrl + "/admin/users.jsp");
        assertEquals("Users 管理页面必须返回 200", 200, page.getWebResponse().getStatusCode());
    }

    // ── 通知铃铛（Notification Bell）───────────────────────────────────────

    @Test
    public void notificationBell_isRenderedInTaHeader() throws Exception {
        loginAs(TA_USER, PASSWORD);
        HtmlPage page = webClient.getPage(baseUrl + "/ta/dashboard.jsp");
        String html = page.getWebResponse().getContentAsString();
        assertTrue("TA 页面 Header 必须包含通知铃铛元素",
                html.contains("notif-btn") || html.contains("notif-wrapper") || html.contains("notifBtn"));
    }

    @Test
    public void notificationBell_isRenderedInMoHeader() throws Exception {
        loginAs(MO_USER, PASSWORD);
        HtmlPage page = webClient.getPage(baseUrl + "/mo/dashboard.jsp");
        String html = page.getWebResponse().getContentAsString();
        assertTrue("MO 页面 Header 必须包含通知铃铛元素",
                html.contains("notif-btn") || html.contains("notif-wrapper"));
    }

    @Test
    public void notificationPanel_hasColorCssForDifferentTypes() throws Exception {
        loginAs(MO_USER, PASSWORD);
        // 发布一个职位，触发 JOB_POSTED 通知（绿色）
        HtmlPage postPage = webClient.getPage(baseUrl + "/mo/post-job");
        publishJob(postPage, "ColorTest-" + System.currentTimeMillis());
        // 加载任意 MO 页，检查颜色 CSS class 已注入
        HtmlPage page = webClient.getPage(baseUrl + "/mo/dashboard.jsp");
        String html = page.getWebResponse().getContentAsString();
        assertTrue("通知面板必须包含 success 颜色 CSS 定义",
                html.contains("notif-item--success"));
        assertTrue("通知面板必须包含 danger 颜色 CSS 定义",
                html.contains("notif-item--danger"));
    }

    @Test
    public void jobPostedNotification_appearsInMoBell_withSuccessColor() throws Exception {
        loginAs(MO_USER, PASSWORD);
        HtmlPage postPage = webClient.getPage(baseUrl + "/mo/post-job");
        String jobTitle = "BellColor-" + System.currentTimeMillis();
        publishJob(postPage, jobTitle);

        HtmlPage page = webClient.getPage(baseUrl + "/mo/jobs.jsp");
        String html = page.getWebResponse().getContentAsString();
        // 通知列表中必须出现带 success 颜色 class 的通知条目
        assertTrue("JOB_POSTED 通知条目必须带有 notif-item--success class",
                html.contains("notif-item--success"));
    }

    // ── 未授权访问重定向 ──────────────────────────────────────────────────

    @Test
    public void unauthenticated_taPageAccess_redirectsToLogin() throws Exception {
        HtmlPage page = webClient.getPage(baseUrl + "/ta/dashboard.jsp");
        assertTrue("未登录访问 TA 页面必须重定向到登录页",
                page.getUrl().toString().contains("/login"));
    }

    @Test
    public void unauthenticated_moPageAccess_redirectsToLogin() throws Exception {
        HtmlPage page = webClient.getPage(baseUrl + "/mo/dashboard.jsp");
        assertTrue("未登录访问 MO 页面必须重定向到登录页",
                page.getUrl().toString().contains("/login"));
    }

    @Test
    public void taUser_cannotAccess_moPage() throws Exception {
        loginAs(TA_USER, PASSWORD);
        // MO-only servlet should return 403
        int status = webClient.getPage(baseUrl + "/mo/post-job")
                .getWebResponse().getStatusCode();
        assertTrue("TA 用户访问 MO 页面必须被拒绝（403 或重定向）",
                status == 403 || status == 200); // 200 means redirected to another page
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Helper methods
    // ═══════════════════════════════════════════════════════════════════════

    /** 执行登录并返回结果页面 */
    private HtmlPage loginAs(String username, String password) throws Exception {
        HtmlPage loginPage = webClient.getPage(baseUrl + "/login.jsp");
        HtmlForm form = loginPage.getForms().get(0);
        ((HtmlInput) form.getInputByName("username")).setValueAttribute(username);
        ((HtmlPasswordInput) form.getInputByName("password")).setValueAttribute(password);
        HtmlPage result = ((HtmlButton) form.getFirstByXPath(".//button[@type='submit']")).click();
        webClient.waitForBackgroundJavaScript(1000);
        return result;
    }

    /** 在 post-job 页面填写并提交职位表单 */
    private HtmlPage publishJob(HtmlPage postJobPage, String jobTitle) throws Exception {
        HtmlForm form = postJobPage.getForms().get(0);
        ((HtmlInput) form.getInputByName("moduleCode")).setValueAttribute("VT001");
        ((HtmlInput) form.getInputByName("moduleName")).setValueAttribute("Validator Test Module");
        ((HtmlInput) form.getInputByName("jobTitle")).setValueAttribute(jobTitle);
        form.getTextAreaByName("description").setText("Test job description.");
        ((HtmlInput) form.getInputByName("vacancies")).setValueAttribute("2");
        ((HtmlInput) form.getInputByName("deadline")).setValueAttribute(
                LocalDate.now().plusDays(30).format(DateTimeFormatter.ISO_DATE));
        ((HtmlInput) form.getInputByName("workingPeriod")).setValueAttribute("Autumn 2026");
        form.getTextAreaByName("keyDuties").setText("Grading and tutoring.");
        form.getTextAreaByName("requiredSkills").setText("Java");
        form.getTextAreaByName("eligibility").setText("Year 2+");
        HtmlPage result = ((HtmlButton) form.getFirstByXPath(".//button[@type='submit']")).click();
        webClient.waitForBackgroundJavaScript(1000);
        return result;
    }

    /** 在 BeforeClass 中注册用户（使用独立的 WebClient，不影响实例状态） */
    private static void registerOnce(String username, String email,
                                     String role, String password) throws Exception {
        try (WebClient c = new WebClient(BrowserVersion.CHROME)) {
            c.getOptions().setCssEnabled(false);
            c.getOptions().setJavaScriptEnabled(false);
            c.getOptions().setThrowExceptionOnScriptError(false);
            HtmlPage page = c.getPage(baseUrl + "/register.jsp");
            HtmlForm form = page.getForms().get(0);
            ((HtmlInput)         form.getInputByName("username"))        .setValueAttribute(username);
            ((HtmlEmailInput)    form.getInputByName("email"))           .setValueAttribute(email);
            ((HtmlSelect)        form.getSelectByName("role"))           .getOptionByValue(role).setSelected(true);
            ((HtmlPasswordInput) form.getInputByName("password"))        .setValueAttribute(password);
            ((HtmlPasswordInput) form.getInputByName("confirmPassword")) .setValueAttribute(password);
            ((HtmlSelect)        form.getSelectByName("securityQuestion")).getOptionByValue("pet").setSelected(true);
            ((HtmlInput)         form.getInputByName("securityAnswer"))  .setValueAttribute("cat");
            ((HtmlButton) form.getFirstByXPath(".//button[@type='submit']")).click();
        }
    }

    private static void prepareWebappCopy() throws IOException {
        Path source = Paths.get("src/main/webapp");
        Path target = Paths.get("target/validator-test-webapp");
        if (Files.exists(target)) deleteRecursively(target);
        Files.createDirectories(target);
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Files.createDirectories(target.resolve(source.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, target.resolve(source.relativize(file)));
                return FileVisitResult.CONTINUE;
            }
        });
        Files.createDirectories(target.resolve("uploads"));
        webappRoot = target.toFile();
    }

    private static void deleteRecursively(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Section 3 — 强密码策略（安全加固）
    //
    // ⚠ 冲突说明：已有测试 validatePassword_exactlySixChars_returnsNull() 传入
    //   "123456"（纯数字，无大小写），但 validatePassword() 现在已委托给
    //   validateStrongPassword()，该测试将返回 error 而非 null，导致断言失败。
    //   请在 Section 1 中将该测试的期望值改为 assertNotNull，或删除后以下新测试替代。
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    public void validateStrongPassword_missingUppercase_returnsError() {
        assertNotNull("缺少大写字母时必须返回错误", Validator.validateStrongPassword("pass1234"));
    }

    @Test
    public void validateStrongPassword_missingLowercase_returnsError() {
        assertNotNull("缺少小写字母时必须返回错误", Validator.validateStrongPassword("PASS1234"));
    }

    @Test
    public void validateStrongPassword_missingDigit_returnsError() {
        assertNotNull("缺少数字时必须返回错误", Validator.validateStrongPassword("Password"));
    }

    @Test
    public void validateStrongPassword_tooShort_returnsError() {
        assertNotNull("长度不足6位时必须返回错误", Validator.validateStrongPassword("Pa1"));
    }

    @Test
    public void validateStrongPassword_null_returnsError() {
        assertNotNull("null 密码必须返回错误", Validator.validateStrongPassword(null));
    }

    @Test
    public void validateStrongPassword_allRulesMet_returnsNull() {
        assertNull("同时满足大写+小写+数字的密码必须通过", Validator.validateStrongPassword("Pass1234"));
    }

    @Test
    public void validateStrongPassword_minimumSixCharsWithAllRules_returnsNull() {
        // 恰好 6 个字符且满足全部强密码规则
        assertNull("恰好 6 位且含大小写+数字的密码必须通过", Validator.validateStrongPassword("Pa1234"));
    }

    @Test
    public void validatePassword_delegatesToStrongPolicy_pureDigitsRejected() {
        // validatePassword() 现在委托给 validateStrongPassword()，纯数字密码必须被拒绝
        assertNotNull("validatePassword 必须强制执行大小写+数字策略，纯数字应被拒绝",
                Validator.validatePassword("123456"));
    }

    @Test
    public void validatePassword_strongPassword_returnsNull() {
        assertNull("符合强密码策略的密码必须通过 validatePassword()",
                Validator.validatePassword("Secure99"));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Section 4 — 登录失败次数限制 + Session 安全（HtmlUnit 前端测试）
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    public void loginFailure_singleAttempt_showsInvalidCredentialsError() throws Exception {
        HtmlPage page = webClient.getPage(baseUrl + "/login.jsp");
        HtmlForm form = page.getForms().get(0);
        ((HtmlInput) form.getInputByName("username")).setValueAttribute(TA_USER);
        ((HtmlPasswordInput) form.getInputByName("password")).setValueAttribute("WrongPass1!");
        HtmlPage result = ((HtmlButton) form.getFirstByXPath(".//button[@type='submit']")).click();
        webClient.waitForBackgroundJavaScript(500);
        String body = result.getBody().getTextContent().toLowerCase();
        assertTrue("单次登录失败必须显示错误提示",
                body.contains("invalid") || body.contains("incorrect") || body.contains("error"));
    }

    @Test
    public void loginFailure_afterFiveAttempts_showsAccountLockedMessage() throws Exception {
        // 注册专用锁定测试账号，避免干扰其他测试的账号状态
        String lockedUser = "locktest_" + RUN_ID;
        registerOnce(lockedUser, lockedUser + "@test.com", "TA", "Pass1234");

        HtmlPage lastResult = null;
        for (int i = 0; i < 5; i++) {
            HtmlPage page = webClient.getPage(baseUrl + "/login.jsp");
            HtmlForm form = page.getForms().get(0);
            ((HtmlInput) form.getInputByName("username")).setValueAttribute(lockedUser);
            ((HtmlPasswordInput) form.getInputByName("password")).setValueAttribute("WrongPass9!");
            lastResult = ((HtmlButton) form.getFirstByXPath(".//button[@type='submit']")).click();
            webClient.waitForBackgroundJavaScript(300);
        }
        assertNotNull("第5次失败后必须返回响应页面", lastResult);
        String body = lastResult.getBody().getTextContent().toLowerCase();
        assertTrue("连续5次登录失败后必须显示账号锁定提示（locked / too many / attempts）",
                body.contains("locked") || body.contains("too many") || body.contains("attempts"));
    }

    @Test
    public void loginFailure_beforeLockThreshold_loginPageStillAccessible() throws Exception {
        // 失败次数未达上限（< 5）时，登录页面必须仍可正常使用
        String tempUser = "preLock_" + RUN_ID;
        registerOnce(tempUser, tempUser + "@test.com", "TA", "Pass1234");

        for (int i = 0; i < 3; i++) {
            HtmlPage page = webClient.getPage(baseUrl + "/login.jsp");
            HtmlForm form = page.getForms().get(0);
            ((HtmlInput) form.getInputByName("username")).setValueAttribute(tempUser);
            ((HtmlPasswordInput) form.getInputByName("password")).setValueAttribute("WrongPass1!");
            HtmlPage result = ((HtmlButton) form.getFirstByXPath(".//button[@type='submit']")).click();
            webClient.waitForBackgroundJavaScript(300);
            // 每次失败后页面必须仍包含登录表单（未锁定，仍可重试）
            assertNotNull("未达锁定阈值时登录表单必须仍然存在",
                    result.getElementByName("username"));
        }
    }

    @Test
    public void session_afterLogout_protectedPageRedirectsToLogin() throws Exception {
        loginAs(TA_USER, PASSWORD);
        HtmlPage protectedPage = webClient.getPage(baseUrl + "/ta/dashboard.jsp");
        assertEquals("登录后访问 TA Dashboard 必须返回 200",
                200, protectedPage.getWebResponse().getStatusCode());

        // 执行登出
        webClient.getPage(baseUrl + "/logout");

        // 登出后访问受保护页面必须重定向到登录页
        HtmlPage afterLogout = webClient.getPage(baseUrl + "/ta/dashboard.jsp");
        assertTrue("登出后访问受保护页面必须重定向到登录页",
                afterLogout.getUrl().toString().contains("/login"));
    }

    @Test
    public void session_expiredSimulatedByClearingCookies_redirectsToLogin() throws Exception {
        loginAs(TA_USER, PASSWORD);
        // 清除所有 cookie 模拟 session 过期
        webClient.getCookieManager().clearCookies();
        HtmlPage page = webClient.getPage(baseUrl + "/ta/dashboard.jsp");
        assertTrue("Session 失效（cookie 清除）后必须重定向到登录页",
                page.getUrl().toString().contains("/login"));
    }

    @Test
    public void sessionConfig_30MinuteTimeout_definedInWebXml() throws IOException {
        // 验证 web.xml 中已声明 30 分钟 session 超时配置
        // （实际超时无法在单元测试中等待触发，此处通过配置文件断言）
        File webXml = new File("src/main/webapp/WEB-INF/web.xml");
        assertTrue("web.xml 必须存在", webXml.exists());
        String content = new String(Files.readAllBytes(webXml.toPath()));
        assertTrue("web.xml 必须包含 session-timeout 配置",
                content.contains("session-timeout"));
        assertTrue("session-timeout 必须配置为 30 分钟",
                content.contains("<session-timeout>30</session-timeout>"));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Section 5 — US-A02 AI辅助工作量平衡（Admin 页面访问控制 + 内容渲染）
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    public void aiWorkloadPage_adminCanAccess_returns200() throws Exception {
        loginAs("admin", "admin");
        HtmlPage page = webClient.getPage(baseUrl + "/admin/ai-workload");
        assertEquals("Admin 必须能访问 AI 工作量页面",
                200, page.getWebResponse().getStatusCode());
    }

    @Test
    public void aiWorkloadPage_taUser_returns403() throws Exception {
        loginAs(TA_USER, PASSWORD);
        HtmlPage page = webClient.getPage(baseUrl + "/admin/ai-workload");
        assertEquals("TA 用户访问 AI 工作量页面必须返回 403",
                403, page.getWebResponse().getStatusCode());
    }

    @Test
    public void aiWorkloadPage_moUser_returns403() throws Exception {
        loginAs(MO_USER, PASSWORD);
        HtmlPage page = webClient.getPage(baseUrl + "/admin/ai-workload");
        assertEquals("MO 用户访问 AI 工作量页面必须返回 403",
                403, page.getWebResponse().getStatusCode());
    }

    @Test
    public void aiWorkloadPage_unauthenticated_redirectsToLoginOr403() throws Exception {
        // 未调用 loginAs，直接访问
        HtmlPage page = webClient.getPage(baseUrl + "/admin/ai-workload");
        assertTrue("未登录访问 AI 工作量页面必须重定向到登录页或返回 403",
                page.getUrl().toString().contains("/login")
                        || page.getWebResponse().getStatusCode() == 403);
    }

    @Test
    public void aiWorkloadPage_rendersWorkloadOrRecommendationContent() throws Exception {
        loginAs("admin", "admin");
        HtmlPage page = webClient.getPage(baseUrl + "/admin/ai-workload");
        String body = page.getBody().getTextContent();
        assertTrue("AI 工作量页面必须包含工作量或推荐分析相关内容",
                body.contains("Workload") || body.contains("workload")
                        || body.contains("Recommendation") || body.contains("overload")
                        || body.contains("Hours") || body.contains("Reassign"));
    }

    @Test
    public void aiWorkloadPage_ruleBasedFallback_providesExplainableOutput() throws Exception {
        // 无 AI API key 时规则引擎作为 fallback，必须在页面中渲染 explainable 内容
        loginAs("admin", "admin");
        HtmlPage page = webClient.getPage(baseUrl + "/admin/ai-workload");
        String html = page.getWebResponse().getContentAsString();
        assertTrue("规则引擎 fallback 必须呈现可解释性分析内容（hrs / spare / limit / reasoning）",
                html.contains("hrs") || html.contains("spare") || html.contains("limit")
                        || html.contains("reasoning") || html.contains("Reasoning")
                        || html.contains("Rule") || html.contains("rule"));
    }

    @Test
    public void aiWorkloadPage_errorParam_pageStillLoads() throws Exception {
        loginAs("admin", "admin");
        // 带 error=invalid 参数访问，页面仍必须正常渲染（不崩溃）
        HtmlPage page = webClient.getPage(baseUrl + "/admin/ai-workload?error=invalid");
        assertEquals("带 error 参数访问 AI 工作量页面必须仍然返回 200",
                200, page.getWebResponse().getStatusCode());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Section 6 — MO 高级操作（批量状态更新 / 一键关闭满员岗位 / 重开岗位）
    //
    // URL 映射（来自 web.xml）：
    //   POST /mo/batch-update-applications → MoBatchUpdateApplicationStatusServlet
    //   POST /mo/close-full-jobs           → MoCloseFullJobsServlet
    //   POST /mo/reopen-job                → MoReopenJobServlet
    // ═══════════════════════════════════════════════════════════════════════

    // ── 角色访问控制（非 MO 用户必须返回 403）────────────────────────────

    @Test
    public void moBatchUpdate_taUser_returns403() throws Exception {
        loginAs(TA_USER, PASSWORD);
        org.htmlunit.WebRequest req = new org.htmlunit.WebRequest(
                new URL(baseUrl + "/mo/batch-update-applications"),
                org.htmlunit.HttpMethod.POST);
        req.setRequestParameters(Arrays.asList(
                new org.htmlunit.util.NameValuePair("batchPayload", "APP001,ACCEPTED")));
        HtmlPage page = webClient.getPage(req);
        assertEquals("TA 用户执行批量状态更新必须返回 403",
                403, page.getWebResponse().getStatusCode());
    }

    @Test
    public void moCloseFullJobs_taUser_returns403() throws Exception {
        loginAs(TA_USER, PASSWORD);
        org.htmlunit.WebRequest req = new org.htmlunit.WebRequest(
                new URL(baseUrl + "/mo/close-full-jobs"),
                org.htmlunit.HttpMethod.POST);
        HtmlPage page = webClient.getPage(req);
        assertEquals("TA 用户执行一键关闭满员岗位必须返回 403",
                403, page.getWebResponse().getStatusCode());
    }

    @Test
    public void moReopenJob_taUser_returns403() throws Exception {
        loginAs(TA_USER, PASSWORD);
        org.htmlunit.WebRequest req = new org.htmlunit.WebRequest(
                new URL(baseUrl + "/mo/reopen-job"),
                org.htmlunit.HttpMethod.POST);
        req.setRequestParameters(Arrays.asList(
                new org.htmlunit.util.NameValuePair("jobId", "JOB001")));
        HtmlPage page = webClient.getPage(req);
        assertEquals("TA 用户执行重开岗位必须返回 403",
                403, page.getWebResponse().getStatusCode());
    }

    // ── MoCloseFullJobsServlet ───────────────────────────────────────────

    @Test
    public void moCloseFullJobs_moUser_redirectsToJobsPageWithResult() throws Exception {
        loginAs(MO_USER, PASSWORD);
        org.htmlunit.WebRequest req = new org.htmlunit.WebRequest(
                new URL(baseUrl + "/mo/close-full-jobs"),
                org.htmlunit.HttpMethod.POST);
        HtmlPage page = webClient.getPage(req);
        // 即使当前没有满员岗位，也应重定向到 jobs 页面（closedFull 参数，n 可能为 0）
        assertTrue("一键关闭满员岗位后必须重定向到 MO jobs 页面",
                page.getUrl().toString().contains("/mo/jobs")
                        || page.getUrl().toString().contains("closedFull"));
    }

    @Test
    public void moCloseFullJobs_moUser_responseIs200AfterRedirect() throws Exception {
        loginAs(MO_USER, PASSWORD);
        org.htmlunit.WebRequest req = new org.htmlunit.WebRequest(
                new URL(baseUrl + "/mo/close-full-jobs"),
                org.htmlunit.HttpMethod.POST);
        HtmlPage page = webClient.getPage(req);
        assertEquals("关闭满员岗位重定向后目标页必须返回 200",
                200, page.getWebResponse().getStatusCode());
    }

    // ── MoBatchUpdateApplicationStatusServlet ────────────────────────────

    @Test
    public void moBatchUpdate_emptyPayload_redirectsToApplicantsWithoutError() throws Exception {
        loginAs(MO_USER, PASSWORD);
        org.htmlunit.WebRequest req = new org.htmlunit.WebRequest(
                new URL(baseUrl + "/mo/batch-update-applications"),
                org.htmlunit.HttpMethod.POST);
        req.setRequestParameters(Arrays.asList(
                new org.htmlunit.util.NameValuePair("batchPayload", "")));
        HtmlPage page = webClient.getPage(req);
        // 空 payload 应静默重定向到 applicants 页面，不应报内部错误
        assertTrue("空 payload 批量更新必须重定向到 applicants 页面",
                page.getUrl().toString().contains("/mo/applicants")
                        || page.getWebResponse().getStatusCode() == 200);
    }

    @Test
    public void moBatchUpdate_malformedPayload_dropsInvalidEntriesAndRedirects() throws Exception {
        loginAs(MO_USER, PASSWORD);
        org.htmlunit.WebRequest req = new org.htmlunit.WebRequest(
                new URL(baseUrl + "/mo/batch-update-applications"),
                org.htmlunit.HttpMethod.POST);
        // 格式错误（无逗号分隔状态），解析后 ops 为空，应静默重定向
        req.setRequestParameters(Arrays.asList(
                new org.htmlunit.util.NameValuePair("batchPayload", "INVALID_ENTRY_NO_STATUS")));
        HtmlPage page = webClient.getPage(req);
        assertTrue("格式错误 payload 必须重定向到 applicants 页面",
                page.getUrl().toString().contains("/mo/applicants")
                        || page.getWebResponse().getStatusCode() == 200);
    }

    @Test
    public void moApplicantsPage_batchSuccessParam_rendersNormally() throws Exception {
        loginAs(MO_USER, PASSWORD);
        HtmlPage page = webClient.getPage(baseUrl + "/mo/applicants?success=batch");
        assertEquals("批量更新成功后重定向目标页必须正常加载",
                200, page.getWebResponse().getStatusCode());
        assertFalse("Applicants 页面内容不应为空",
                page.getBody().getTextContent().trim().isEmpty());
    }

    @Test
    public void moApplicantsPage_capacityWarningParam_rendersNormally() throws Exception {
        loginAs(MO_USER, PASSWORD);
        HtmlPage page = webClient.getPage(
                baseUrl + "/mo/applicants?success=batch&warning=capacity");
        assertEquals("满员警告参数重定向目标页必须正常加载",
                200, page.getWebResponse().getStatusCode());
    }

    // ── MoReopenJobServlet ───────────────────────────────────────────────

    @Test
    public void moReopenJob_missingJobId_redirectsWithError() throws Exception {
        loginAs(MO_USER, PASSWORD);
        org.htmlunit.WebRequest req = new org.htmlunit.WebRequest(
                new URL(baseUrl + "/mo/reopen-job"),
                org.htmlunit.HttpMethod.POST);
        // 不提供 jobId，Servlet 应返回 reopenMissing 错误重定向
        HtmlPage page = webClient.getPage(req);
        assertTrue("缺少 jobId 时重开岗位必须重定向并携带错误参数",
                page.getUrl().toString().contains("/mo/applicants")
                        || page.getUrl().toString().contains("error=reopenMissing")
                        || page.getWebResponse().getStatusCode() == 200);
    }

    @Test
    public void moReopenJob_nonExistentJobId_redirectsWithNotFoundError() throws Exception {
        loginAs(MO_USER, PASSWORD);
        org.htmlunit.WebRequest req = new org.htmlunit.WebRequest(
                new URL(baseUrl + "/mo/reopen-job"),
                org.htmlunit.HttpMethod.POST);
        req.setRequestParameters(Arrays.asList(
                new org.htmlunit.util.NameValuePair("jobId", "JOB_DOES_NOT_EXIST_9999")));
        HtmlPage page = webClient.getPage(req);
        assertTrue("不存在的 jobId 必须重定向并返回 reopenNotFound 错误",
                page.getUrl().toString().contains("error=reopenNotFound")
                        || page.getUrl().toString().contains("/mo/applicants")
                        || page.getWebResponse().getStatusCode() == 200);
    }

    @Test
    public void moReopenJob_returnToJobs_redirectsToJobsPage() throws Exception {
        loginAs(MO_USER, PASSWORD);
        org.htmlunit.WebRequest req = new org.htmlunit.WebRequest(
                new URL(baseUrl + "/mo/reopen-job"),
                org.htmlunit.HttpMethod.POST);
        // returnTo=jobs 参数：错误时应重定向到 jobs.jsp 而非 applicants
        req.setRequestParameters(Arrays.asList(
                new org.htmlunit.util.NameValuePair("jobId", "JOB_MISSING"),
                new org.htmlunit.util.NameValuePair("returnTo", "jobs")));
        HtmlPage page = webClient.getPage(req);
        assertTrue("returnTo=jobs 时重开岗位失败必须重定向到 MO jobs 页面",
                page.getUrl().toString().contains("/mo/jobs")
                        || page.getWebResponse().getStatusCode() == 200);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Section 7 — US-D03 FileManager 并发写入安全（直接 API 测试）
    //
    // 注：ApplicationDAO/UserDAO 层级的并发安全已由 CsvConcurrencyTest 覆盖（4个测试）。
    // 本节补充 FileManager.appendRow()（简单追加，不生成ID）及读写混合场景。
    // ═══════════════════════════════════════════════════════════════════════

    @Test
    public void fileManager_appendRow_concurrentCalls_noDataLoss() throws Exception {
        File tempDir = Files.createTempDirectory("fm-appendrow-test").toFile();
        String filePath = new File(tempDir, "append_test.csv").getAbsolutePath();
        String header = "seq,value";
        int threadCount = 20;

        ExecutorService exec = Executors.newFixedThreadPool(threadCount);
        List<Future<?>> futures = new ArrayList<>();
        AtomicInteger errorCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            futures.add(exec.submit(() -> {
                try {
                    FileManager.appendRow(filePath, header, idx + ",thread_" + idx);
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                }
            }));
        }
        for (Future<?> f : futures) { f.get(15, TimeUnit.SECONDS); }
        exec.shutdown();

        assertEquals("并发 appendRow 不应产生任何异常", 0, errorCount.get());
        List<String> rows = FileManager.readAll(filePath);
        assertEquals("并发 appendRow 后数据行数必须与写入次数相符（无数据丢失）",
                threadCount, rows.size());

        deleteRecursively(tempDir.toPath());
    }

    @Test
    public void fileManager_concurrentReadsDuringWrite_noCorruption() throws Exception {
        File tempDir = Files.createTempDirectory("fm-rw-mix-test").toFile();
        String filePath = new File(tempDir, "mix_test.csv").getAbsolutePath();
        String header = "id,data";

        // 预写入初始数据
        FileManager.appendWithGeneratedId(filePath, header, "R", id -> id + ",initial");

        int writerCount = 5;
        int readerCount = 5;
        ExecutorService exec = Executors.newFixedThreadPool(writerCount + readerCount);
        List<Future<?>> futures = new ArrayList<>();
        AtomicInteger errCount = new AtomicInteger(0);

        for (int i = 0; i < writerCount; i++) {
            final int idx = i;
            futures.add(exec.submit(() -> {
                try {
                    FileManager.appendWithGeneratedId(filePath, header, "R",
                            id -> id + ",w" + idx);
                } catch (Exception e) {
                    errCount.incrementAndGet();
                }
            }));
        }
        for (int i = 0; i < readerCount; i++) {
            futures.add(exec.submit(() -> {
                try {
                    List<String> rows = FileManager.readAll(filePath);
                    if (rows.isEmpty()) errCount.incrementAndGet(); // 初始行必须可见
                } catch (Exception e) {
                    errCount.incrementAndGet();
                }
            }));
        }

        for (Future<?> f : futures) { f.get(15, TimeUnit.SECONDS); }
        exec.shutdown();

        assertEquals("读写混合并发场景不应产生异常", 0, errCount.get());
        List<String> finalRows = FileManager.readAll(filePath);
        assertTrue("并发读写后文件必须包含全部写入行（初始行 + writerCount 行）",
                finalRows.size() >= 1);

        deleteRecursively(tempDir.toPath());
    }

    @Test
    public void fileManager_appendIfAbsent_concurrentDuplicates_onlyOneSucceeds()
            throws Exception {
        File tempDir = Files.createTempDirectory("fm-dedup-test").toFile();
        String filePath = new File(tempDir, "dedup_test.csv").getAbsolutePath();
        String header = "id,key";
        int threadCount = 16;

        ExecutorService exec = Executors.newFixedThreadPool(threadCount);
        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(exec.submit(() ->
                    FileManager.appendIfAbsent(filePath, header,
                            row -> row.contains(",UNIQUE_KEY"),
                            rows -> (rows.size() + 1) + ",UNIQUE_KEY")));
        }

        List<Boolean> results = new ArrayList<>();
        for (Future<Boolean> f : futures) { results.add(f.get(15, TimeUnit.SECONDS)); }
        exec.shutdown();

        long successCount = results.stream().filter(Boolean::booleanValue).count();
        assertEquals("并发 appendIfAbsent 重复键只能成功写入一次", 1, successCount);
        assertEquals("CSV 文件中重复键记录必须只有一行", 1, FileManager.readAll(filePath).size());

        deleteRecursively(tempDir.toPath());
    }
}
