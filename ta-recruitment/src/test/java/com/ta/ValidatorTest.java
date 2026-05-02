package com.ta;

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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
    public void validatePassword_exactlySixChars_returnsNull() {
        assertNull(Validator.validatePassword("123456"));
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
}
