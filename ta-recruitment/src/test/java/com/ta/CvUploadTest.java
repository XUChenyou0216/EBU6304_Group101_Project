package com.ta;

import com.ta.dao.TAProfileDAO;
import com.ta.model.TAProfile;
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
import org.htmlunit.html.HtmlFileInput;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlInput;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlPasswordInput;
import org.htmlunit.html.HtmlSelect;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static org.junit.Assert.*;

/**

 * Integration and unit tests for user story <strong>US-T02: CV Upload Security Validation</strong>.
 *
 * <p>As a tester, these tests verify that the system correctly blocks unsupported file types
 * or oversized files when a teaching assistant (TA) uploads a CV, so that the application
 * does not crash or persist malicious content.</p>
 *
 * <h2>Acceptance Criteria</h2>
 * <ul>
 *   <li><strong>AC-1</strong> — Executable files (e.g. {@code .exe}, {@code .sh}) and files
 *       larger than 10&nbsp;MB are rejected.</li>
 *   <li><strong>AC-2</strong> — A clear error message is returned for invalid uploads.</li>
 *   <li><strong>AC-3</strong> — Only standard document formats ({@code .pdf}, {@code .doc},
 *       {@code .docx}) are saved to the local uploads directory; rejected files never reach
 *       the file system.</li>
 * </ul>
 *
 * <h2>Test Structure</h2>
 * <ul>
 *   <li><strong>Sections 1–3</strong> — Direct {@link Validator} unit tests and file-system
 *       simulations using JUnit 4 {@link TemporaryFolder}. These sections exercise acceptance
 *       criteria AC-1 through AC-3 without a running servlet container.</li>
 *   <li><strong>Section 4 — HTTP servlet layer</strong> — End-to-end upload tests with embedded
 *       Tomcat and HtmlUnit. A TA user logs in, selects a file on {@code profile.jsp}, submits
 *       the form, and the test asserts that {@code UploadCvServlet} redirects to the expected
 *       query string ({@code ?error=invalid_format}, {@code ?uploadStatus=success}, or
 *       {@code /login} when unauthenticated).</li>
 *   <li><strong>Section 5 — UI layer</strong> — Loads {@code profile.jsp} directly with error or
 *       success query parameters and verifies that JSP-rendered banners, styling, hint text, and
 *       the file input {@code accept} attribute match the security policy (PDF/DOC/DOCX only;
 *       no JPG/PNG).</li>
 * </ul>
 *
 * <h2>Test Infrastructure</h2>
 * <ul>
 *   <li><strong>Embedded Tomcat</strong> — A temporary web application is copied from
 *       {@code src/main/webapp} into {@code target/cv-upload-webapp}, with compiled classes
 *       served from {@code target/classes}. Tomcat binds to a random port for isolation.</li>
 *   <li><strong>HtmlUnit</strong> — Simulates browser login, form submission, and file uploads
 *       against the running servlet container.</li>
 *   <li><strong>Dedicated TA account</strong> — A unique TA user is registered once per test
 *       run (username prefix {@code cvtest_ta_}) for authenticated servlet and UI scenarios.</li>
 *   <li><strong>Size constants</strong> — The 10&nbsp;MB upload limit is expressed as
 *       {@code MAX_SIZE = 10 * 1024 * 1024} bytes for boundary testing.</li>
 * </ul>
 *
 * <p>Run with: {@code mvn -Dtest=CvUploadTest test}</p>
 *
 * @see Validator
 * @see TAProfileDAO

 * US-T02: CV Upload Security Validation (Should)
 *
 * As a Tester, I want to verify the system correctly blocks unsupported file
 * types or oversized files when a TA uploads a CV, so that the application
 * doesn't crash or store malicious files.
 *
 * Acceptance Criteria:
 *   AC-1  Executable files (.exe, .sh) and files larger than 10 MB are rejected.
 *   AC-2  A clear error message is returned for invalid uploads.
 *   AC-3  Only standard document formats (.pdf, .doc, .docx) are saved to the
 *         local uploads directory; rejected files never reach the file system.
 *
 * Run with: mvn -Dtest=CvUploadTest test

 */
public class CvUploadTest {

    private static final long KB       = 1024L;
    private static final long MB       = 1024L * KB;
    private static final long MAX_SIZE = 10L * MB;


    // ── JUnit 4 TemporaryFolder (Sections 1–3 file-system tests) ────────────
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    // ── Embedded Tomcat (Sections 4–5 servlet/UI tests) ───────────────────────

    // ── JUnit 4 TemporaryFolder（Section 1-3 文件系统测试用）────────────────
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    // ── 嵌入式 Tomcat（Section 4-5 Servlet/UI 测试用）────────────────────────

    private static Tomcat   tomcat;
    private static String   baseUrl;
    private static File     webappRoot;
    private        WebClient webClient;

    private static final long   RUN_ID      = System.currentTimeMillis();
    private static final String TA_USER     = "cvtest_ta_" + RUN_ID;
    private static final String TA_PASSWORD = "Pass1234";


    /**
     * Starts an embedded Tomcat server before any test in this class runs.
     *
     * <p>Copies the web application from {@code src/main/webapp} into a temporary build
     * directory, wires compiled classes from {@code target/classes}, picks a random port,
     * and registers a dedicated TA test account for servlet and UI scenarios.</p>
     *
     * <p>This method is invoked once per test class by JUnit 4 and must complete
     * successfully before any {@link Test} method in Sections 4–5 can execute.</p>
     *
     * @throws Exception if the webapp copy, Tomcat startup, or TA registration fails
     */

    @BeforeClass
    public static void startServer() throws Exception {
        prepareWebappCopy();
        tomcat = new Tomcat();
        tomcat.setPort(0);
        tomcat.setBaseDir("target/cv-upload-tomcat");
        Context ctx = tomcat.addWebapp("", webappRoot.getAbsolutePath());
        ctx.setParentClassLoader(Thread.currentThread().getContextClassLoader());
        WebResourceRoot res = new StandardRoot(ctx);
        res.addPreResources(new DirResourceSet(res, "/WEB-INF/classes",
                new File("target/classes").getAbsolutePath(), "/"));
        ctx.setResources(res);
        tomcat.start();
        baseUrl = "http://localhost:" + tomcat.getConnector().getLocalPort();
        registerTA(TA_USER, TA_USER + "@test.com", TA_PASSWORD);
    }


    /**
     * Stops and destroys the embedded Tomcat server after all tests in this class complete.
     *
     * <p>Releases the servlet container and network resources allocated by
     * {@link #startServer()}. Safe to call even if Tomcat was never started.</p>
     *
     * @throws Exception if Tomcat shutdown fails
     */

    @AfterClass
    public static void stopServer() throws Exception {
        if (tomcat != null) { tomcat.stop(); tomcat.destroy(); }
    }


    /**
     * Creates a fresh HtmlUnit {@link WebClient} before each test method.
     *
     * <p>JavaScript is enabled for servlet redirect tests; CSS and script errors are suppressed
     * so that UI assertions focus on content rather than styling engine noise. Redirect
     * following is enabled so that post-upload navigation can be inspected via the final URL.</p>
     */

    @Before
    public void openBrowser() {
        webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setRedirectEnabled(true);
    }


    /**
     * Closes the HtmlUnit {@link WebClient} after each test method to release resources.
     *
     * <p>Prevents connection leaks between test methods by shutting down the browser
     * instance created in {@link #openBrowser()}.</p>
     */

    @After
    public void closeBrowser() {
        if (webClient != null) webClient.close();
    }

    // ── AC-1: Blocked file types ──────────────────────────────────────────


    /**
     * Verifies that a Windows executable ({@code .exe}) is rejected by
     * {@link Validator#validateCvFile(String, long)}.
     *
     * <p>Covers <strong>AC-1</strong>: executable file types must not be accepted as CV uploads,
     * regardless of file size.</p>
     */

    @Test
    public void executableFile_exe_isRejected() {
        String error = Validator.validateCvFile("malware.exe", MB);
        assertNotNull("EXE file must be rejected by the validator", error);
    }


    /**
     * Verifies that a Unix shell script ({@code .sh}) is rejected by
     * {@link Validator#validateCvFile(String, long)}.
     *
     * <p>Covers <strong>AC-1</strong>: script files that could execute arbitrary commands
     * must be blocked at validation time.</p>
     */

    @Test
    public void executableFile_sh_isRejected() {
        String error = Validator.validateCvFile("deploy.sh", MB);
        assertNotNull("Shell script (.sh) must be rejected by the validator", error);
    }


    /**
     * Verifies that a Windows batch script ({@code .bat}) is rejected by
     * {@link Validator#validateCvFile(String, long)}.
     *
     * <p>Covers <strong>AC-1</strong>: Windows batch files are treated as executable content
     * and must be rejected.</p>
     */

    @Test
    public void executableFile_bat_isRejected() {
        String error = Validator.validateCvFile("run.bat", MB);
        assertNotNull("Batch script (.bat) must be rejected", error);
    }


    /**
     * Verifies that a ZIP archive ({@code .zip}) is rejected by
     * {@link Validator#validateCvFile(String, long)}.
     *
     * <p>Covers <strong>AC-1</strong>: compressed archives are not among the allowed document
     * formats and must be rejected.</p>
     */

    @Test
    public void compressedFile_zip_isRejected() {
        String error = Validator.validateCvFile("archive.zip", MB);
        assertNotNull("ZIP archive must be rejected", error);
    }


    /**
     * Verifies that a JavaScript file ({@code .js}) is rejected by
     * {@link Validator#validateCvFile(String, long)}.
     *
     * <p>Covers <strong>AC-1</strong>: client-side script files must not be accepted as CV
     * uploads.</p>
     */

    @Test
    public void scriptFile_js_isRejected() {
        String error = Validator.validateCvFile("script.js", MB);
        assertNotNull("JavaScript file must be rejected", error);
    }


    /**
     * Verifies that a PDF file whose size exceeds the 10&nbsp;MB limit is rejected by
     * {@link Validator#validateCvFile(String, long)}.
     *
     * <p>Covers <strong>AC-1</strong>: even a valid extension cannot bypass the maximum
     * file size constraint.</p>
     */

    @Test
    public void fileExceedingTenMb_isRejected() {
        // MAX_SIZE + 1 byte exceeds the limit
        String error = Validator.validateCvFile("cv.pdf", MAX_SIZE + 1);
        assertNotNull("A file larger than 10 MB must be rejected", error);
    }


    /**
     * Verifies that an oversized executable is rejected even when both format and size
     * would independently fail validation.
     *
     * <p>Covers <strong>AC-1</strong>: a file that violates both the extension whitelist
     * and the size limit must still produce a validation error.</p>
     */

    @Test
    public void largeExecutable_isRejectedForBothReasons() {
        // Even if the file were somehow labelled valid, size alone also fails
        String formatError = Validator.validateCvFile("huge.exe", MAX_SIZE + 1);
        assertNotNull("Oversized EXE must be rejected", formatError);
    }

    // ── AC-1 boundary: size edge cases ────────────────────────────────────


    /**
     * Verifies that a PDF file whose size equals exactly 10&nbsp;MB (the configured limit)
     * passes validation.
     *
     * <p>Covers the inclusive upper boundary of <strong>AC-1</strong>: a file at exactly
     * {@code MAX_SIZE} bytes must be accepted because the check uses strict greater-than.</p>
     */

    @Test
    public void fileSizeAtExactLimit_isAccepted() {
        // fileSize == 10 * 1024 * 1024 is NOT greater-than, so it must pass
        String error = Validator.validateCvFile("cv.pdf", MAX_SIZE);
        assertNull("A PDF file of exactly 10 MB should be accepted", error);
    }


    /**
     * Verifies that a DOCX file one byte below the 10&nbsp;MB limit passes validation.
     *
     * <p>Covers a near-limit boundary case just below {@code MAX_SIZE} to confirm that
     * files slightly under the cap are accepted.</p>
     */

    @Test
    public void fileSizeOneByteBelowLimit_isAccepted() {
        String error = Validator.validateCvFile("cv.docx", MAX_SIZE - 1);
        assertNull("A DOCX file one byte below 10 MB should be accepted", error);
    }


    /**
     * Verifies that a PDF file one byte above the 10&nbsp;MB limit is rejected.
     *
     * <p>Covers the exclusive upper boundary of <strong>AC-1</strong>: a file of
     * {@code MAX_SIZE + 1} bytes must fail validation.</p>
     */

    @Test
    public void fileSizeOneBytAboveLimit_isRejected() {
        String error = Validator.validateCvFile("cv.pdf", MAX_SIZE + 1);
        assertNotNull("A PDF file one byte above 10 MB must be rejected", error);
    }

    // ── AC-2: Clear error messages ────────────────────────────────────────


    /**
     * Verifies that the error message for an invalid file format mentions PDF as an
     * allowed type.
     *
     * <p>Covers <strong>AC-2</strong>: users must receive guidance listing PDF as a
     * supported format when they upload a disallowed file type.</p>
     */

    @Test
    public void invalidFormat_errorMessageMentionsPdf() {
        String error = Validator.validateCvFile("resume.exe", MB);
        assertNotNull(error);
        assertTrue(
            "Error message for invalid format must mention PDF",
            error.toUpperCase().contains("PDF")
        );
    }


    /**
     * Verifies that the error message for an invalid file format mentions DOC or DOCX as
     * allowed types.
     *
     * <p>Covers <strong>AC-2</strong>: the error text must inform users that Word document
     * formats are permitted.</p>
     */

    @Test
    public void invalidFormat_errorMessageMentionsDoc() {
        String error = Validator.validateCvFile("resume.sh", MB);
        assertNotNull(error);
        assertTrue(
            "Error message for invalid format must mention DOC or DOCX",
            error.toUpperCase().contains("DOC")
        );
    }


    /**
     * Verifies that the error message for an invalid file format begins with
     * {@code "Invalid file format"}.
     *
     * <p>Covers <strong>AC-2</strong>: the primary error prefix must clearly identify
     * the failure as a format problem rather than a size or server error.</p>
     */

    @Test
    public void invalidFormat_errorMessageContainsInvalidFileFormat() {
        String error = Validator.validateCvFile("payload.exe", MB);
        assertNotNull(error);
        assertTrue(
            "Error message must start with 'Invalid file format'",
            error.startsWith("Invalid file format")
        );
    }


    /**
     * Verifies that the error message for an oversized file references the size limit
     * (e.g. {@code "10"}, {@code "limit"}, or {@code "exceed"}).
     *
     * <p>Covers <strong>AC-2</strong>: users uploading files that exceed 10&nbsp;MB must
     * receive a message that explains the size restriction.</p>
     */

    @Test
    public void oversizedFile_errorMessageMentionsSizeLimit() {
        String error = Validator.validateCvFile("cv.pdf", MAX_SIZE + 1);
        assertNotNull(error);
        assertTrue(
            "Error message for oversized file must mention 10 MB or the limit",
            error.contains("10") || error.toLowerCase().contains("limit")
                    || error.toLowerCase().contains("exceed")
        );
    }


    /**
     * Verifies that a {@code null} filename produces a non-null validation error
     * (prompting the user to select a file).
     *
     * <p>Covers <strong>AC-2</strong>: missing file selection must yield a user-facing
     * error rather than a silent pass or uncaught failure.</p>
     */

    @Test
    public void nullFileName_returnsSelectFileError() {
        String error = Validator.validateCvFile(null, MB);
        assertNotNull("Null filename must return a non-null error", error);
    }


    /**
     * Verifies that an empty filename produces a non-null validation error.
     *
     * <p>Covers <strong>AC-2</strong>: an empty string filename is treated as no file
     * selected and must return an error message.</p>
     */

    @Test
    public void emptyFileName_returnsSelectFileError() {
        String error = Validator.validateCvFile("", MB);
        assertNotNull("Empty filename must return a non-null error", error);
    }

    // ── AC-3: Valid formats pass validation ───────────────────────────────


    /**
     * Verifies that a PDF file within the size limit passes validation.
     *
     * <p>Covers <strong>AC-3</strong>: {@code .pdf} is an allowed document format when
     * the file size is within the 10&nbsp;MB cap.</p>
     */

    @Test
    public void validPdfFile_passesValidation() {
        assertNull(
            "A PDF file within the size limit must pass validation",
            Validator.validateCvFile("cv.pdf", 500 * KB)
        );
    }


    /**
     * Verifies that a DOCX file within the size limit passes validation.
     *
     * <p>Covers <strong>AC-3</strong>: {@code .docx} is an allowed document format when
     * the file size is within the 10&nbsp;MB cap.</p>
     */

    @Test
    public void validDocxFile_passesValidation() {
        assertNull(
            "A DOCX file within the size limit must pass validation",
            Validator.validateCvFile("cv.docx", 500 * KB)
        );
    }


    /**
     * Verifies that a DOC file within the size limit passes validation.
     *
     * <p>Covers <strong>AC-3</strong>: legacy {@code .doc} Word files are permitted when
     * within the size limit.</p>
     */

    @Test
    public void validDocFile_passesValidation() {
        assertNull(
            "A DOC file within the size limit must pass validation",
            Validator.validateCvFile("cv.doc", 500 * KB)
        );
    }


    /**
     * Verifies that the PDF extension check is case-insensitive (e.g. {@code CV.PDF}).
     *
     * <p>Covers <strong>AC-3</strong>: uppercase extensions must be normalised and accepted
     * because {@link Validator} lowercases the filename internally.</p>
     */

    @Test
    public void pdfExtension_isCaseInsensitive() {
        // Validator lowercases the name internally, so CV.PDF must also pass
        assertNull(
            "PDF with uppercase extension must also be accepted",
            Validator.validateCvFile("CV.PDF", 500 * KB)
        );
    }


    /**
     * Verifies that the DOCX extension check is case-insensitive (e.g. {@code Resume.DOCX}).
     *
     * <p>Covers <strong>AC-3</strong>: mixed-case DOCX filenames from different operating
     * systems must pass validation.</p>
     */

    @Test
    public void docxExtension_isCaseInsensitive() {
        assertNull(
            "DOCX with uppercase extension must also be accepted",
            Validator.validateCvFile("Resume.DOCX", 500 * KB)
        );
    }

    // ── AC-3: Reject silently — file is never written to disk ─────────────


    /**
     * Simulates the upload guard: an executable must be rejected before any bytes are written
     * to the uploads directory.
     *
     * <p>Covers <strong>AC-3</strong>: mirrors the servlet guard pattern — validation runs
     * before {@link Files#write}, so a rejected {@code .exe} must never appear on disk.</p>
     *
     * @throws Exception if temporary folder creation or file I/O fails unexpectedly
     */

    @Test
    public void executableFile_isNeverWrittenToUploadsDir() throws Exception {
        File uploadsDir = folder.newFolder("uploads");
        String fileName = "virus.exe";

        // Simulate the guard: only write if validation passes
        String error = Validator.validateCvFile(fileName, MB);
        if (error == null) {
            // If validator unexpectedly passes, write the file (this path must NOT run)
            Path target = Paths.get(uploadsDir.getAbsolutePath(), fileName);
            Files.write(target, new byte[]{0x4D, 0x5A}); // MZ header
        }

        assertNotNull("Validator must reject the EXE before any disk write", error);
        assertFalse(
            "virus.exe must not exist in the uploads directory",
            Paths.get(uploadsDir.getAbsolutePath(), fileName).toFile().exists()
        );
    }


    /**
     * Simulates the upload guard: a shell script must be rejected before any bytes are
     * written to the uploads directory.
     *
     * <p>Covers <strong>AC-3</strong>: a {@code .sh} file that fails validation must not
     * be persisted even if the upload handler would otherwise write bytes.</p>
     *
     * @throws Exception if temporary folder creation or file I/O fails unexpectedly
     */

    @Test
    public void shellScript_isNeverWrittenToUploadsDir() throws Exception {
        File uploadsDir = folder.newFolder("uploads");
        String fileName = "exploit.sh";

        String error = Validator.validateCvFile(fileName, MB);
        if (error == null) {
            Path target = Paths.get(uploadsDir.getAbsolutePath(), fileName);
            Files.write(target, "#!/bin/sh\nrm -rf /".getBytes());
        }

        assertNotNull("Validator must reject the shell script before any disk write", error);
        assertFalse(
            "exploit.sh must not exist in the uploads directory",
            Paths.get(uploadsDir.getAbsolutePath(), fileName).toFile().exists()
        );
    }

    // ── AC-3: Valid documents are saved and recorded in profiles.csv ──────


    /**
     * Verifies end-to-end persistence for a valid PDF: the file is written to disk and the
     * TA profile record stores a {@code .pdf} CV path.
     *
     * <p>Covers <strong>AC-3</strong>: after validation passes, the upload flow must save
     * the file to the uploads directory and record the relative path via
     * {@link TAProfileDAO#saveOrUpdate(TAProfile)}.</p>
     *
     * @throws Exception if temporary folder creation, file I/O, or DAO operations fail
     */

    @Test
    public void validPdfFile_isSavedAndProfileRecorded() throws Exception {
        File uploadsDir = folder.newFolder("uploads");
        File dataDir    = folder.newFolder("data");

        String fileName = "U300_" + System.currentTimeMillis() + ".pdf";
        Path   target   = Paths.get(uploadsDir.getAbsolutePath(), fileName);

        // Guard: validate before writing
        String error = Validator.validateCvFile(fileName, 200 * KB);
        assertNull("PDF must pass validation", error);

        // Write a minimal valid PDF (first four bytes are the PDF magic number)
        Files.write(target, new byte[]{0x25, 0x50, 0x44, 0x46});

        // Persist the profile record (mirrors UploadCvServlet logic)
        TAProfileDAO dao     = new TAProfileDAO(dataDir.getAbsolutePath());
        TAProfile    profile = new TAProfile(
                "U300", "2024310001", "Chen Jia",
                "Software Engineering", "Year 2", "13900001111",
                "uploads/" + fileName);
        dao.saveOrUpdate(profile);

        // Assertions
        assertTrue("PDF file must exist on disk after save",     Files.exists(target));
        TAProfile saved = dao.findByUserId("U300");
        assertNotNull("TAProfile must be persisted",             saved);
        assertTrue("Stored CV path must end with .pdf",
                saved.getCvFilePath().endsWith(".pdf"));
    }


    /**
     * Verifies end-to-end persistence for a valid DOCX: the file is written to disk and the
     * TA profile record stores a {@code .docx} CV path.
     *
     * <p>Covers <strong>AC-3</strong>: DOCX uploads follow the same persistence path as PDF,
     * storing the file on disk and updating the TA profile record.</p>
     *
     * @throws Exception if temporary folder creation, file I/O, or DAO operations fail
     */

    @Test
    public void validDocxFile_isSavedAndProfileRecorded() throws Exception {
        File uploadsDir = folder.newFolder("uploads");
        File dataDir    = folder.newFolder("data");

        String fileName = "U301_" + System.currentTimeMillis() + ".docx";
        Path   target   = Paths.get(uploadsDir.getAbsolutePath(), fileName);

        String error = Validator.validateCvFile(fileName, 300 * KB);
        assertNull("DOCX must pass validation", error);

        Files.write(target, new byte[1024]); // placeholder content

        TAProfileDAO dao     = new TAProfileDAO(dataDir.getAbsolutePath());
        TAProfile    profile = new TAProfile(
                "U301", "2024310002", "Wang Li",
                "Electrical Engineering", "Year 3", "13800002222",
                "uploads/" + fileName);
        dao.saveOrUpdate(profile);

        assertTrue("DOCX file must exist on disk after save",   Files.exists(target));
        TAProfile saved = dao.findByUserId("U301");
        assertNotNull("TAProfile must be persisted",            saved);
        assertTrue("Stored CV path must end with .docx",
                saved.getCvFilePath().endsWith(".docx"));
    }


    /**
     * Verifies that a legitimately saved profile CV path never contains a dangerous extension
     * and ends with an allowed document suffix.
     *
     * <p>Covers <strong>AC-3</strong>: persisted CV paths in the profile store must only
     * reference safe document extensions ({@code .pdf}, {@code .doc}, {@code .docx}).</p>
     *
     * @throws Exception if temporary folder creation or DAO operations fail
     */

    @Test
    public void profileCvPath_neverContainsDangerousExtension() throws Exception {
        File dataDir = folder.newFolder("data");
        TAProfileDAO dao = new TAProfileDAO(dataDir.getAbsolutePath());

        // Save a profile that was legitimately created via a PDF upload
        TAProfile profile = new TAProfile(
                "U302", "2024310003", "Zhang Wei",
                "Computer Science", "Year 1", "13700003333",
                "uploads/U302_1234567890.pdf");
        dao.saveOrUpdate(profile);

        TAProfile saved = dao.findByUserId("U302");
        assertNotNull(saved);

        String cvPath = saved.getCvFilePath();
        assertFalse("Stored path must not end with .exe", cvPath.endsWith(".exe"));
        assertFalse("Stored path must not end with .sh",  cvPath.endsWith(".sh"));
        assertFalse("Stored path must not end with .bat", cvPath.endsWith(".bat"));
        assertTrue("Stored path must end with an allowed extension (.pdf)",
                cvPath.endsWith(".pdf") || cvPath.endsWith(".docx") || cvPath.endsWith(".doc"));
    }

    // ── Multi-case sweep ──────────────────────────────────────────────────


    /**
     * Parameterised sweep asserting that a broad set of dangerous or disallowed extensions
     * are all rejected by {@link Validator#validateCvFile(String, long)}.
     *
     * <p>Covers <strong>AC-1</strong> across many common executable, script, archive, and
     * non-document extensions in a single loop to guard against whitelist gaps.</p>
     */

    @Test
    public void allCommonDangerousExtensions_areRejected() {
        String[] dangerous = {"exe", "sh", "bat", "cmd", "msi", "vbs",
                              "ps1", "py", "js", "jar", "zip", "tar", "rar"};
        for (String ext : dangerous) {
            String error = Validator.validateCvFile("file." + ext, 100 * KB);
            assertNotNull(
                "File with extension '." + ext + "' must be rejected, but was accepted",
                error
            );
        }
    }

    // ═══════════════════════════════════════════════════════════════════════

    // Section 4 — HTTP servlet layer: real file upload → redirect URL verification
    //   Embedded Tomcat + HtmlUnit; exercises UploadCvServlet redirect behaviour
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Uploads a small {@code .exe} file through the profile page and asserts that
     * {@code UploadCvServlet} redirects to {@code ?error=invalid_format}.
     *
     * <p>Exercises the full HTTP upload path (Section 4): login, select file on
     * {@code profile.jsp}, submit, and verify the post-upload redirect URL contains
     * the invalid-format error code.</p>
     *
     * @throws Exception if login, page navigation, or file upload simulation fails
     */

    // Section 4 — HTTP Servlet 层：真实文件上传 → 重定向 URL 验证
    //   使用嵌入式 Tomcat + HtmlUnit，测试 UploadCvServlet 的重定向逻辑
    // ═══════════════════════════════════════════════════════════════════════


    @Test
    public void uploadExeFile_servletRedirectsToInvalidFormatError() throws Exception {
        loginAs(TA_USER, TA_PASSWORD);

        // 创建一个小的 .exe 测试文件
        File exeFile = folder.newFile("virus.exe");
        Files.write(exeFile.toPath(), new byte[]{0x4D, 0x5A}); // MZ header

        HtmlPage profilePage = webClient.getPage(baseUrl + "/ta/profile.jsp");
        HtmlFileInput fileInput = profilePage.getHtmlElementById("cvFile");
        fileInput.setFiles(exeFile);
        webClient.waitForBackgroundJavaScript(2000);

        HtmlPage result = (HtmlPage) webClient.getCurrentWindow().getEnclosedPage();
        String url = result.getUrl().toString();
        assertTrue(
            "上传 .exe 文件后，Servlet 必须重定向到 ?error=invalid_format，实际 URL: " + url,
            url.contains("error=invalid_format")
        );
    }


    /**
     * Uploads a shell script through the profile page and asserts that the servlet redirects
     * to {@code ?error=invalid_format}.
     *
     * <p>Exercises the full HTTP upload path (Section 4) for a Unix shell script, confirming
     * that script uploads are blocked at the servlet layer with the same error code as
     * executables.</p>
     *
     * @throws Exception if login, page navigation, or file upload simulation fails
     */

    @Test
    public void uploadShFile_servletRedirectsToInvalidFormatError() throws Exception {
        loginAs(TA_USER, TA_PASSWORD);

        File shFile = folder.newFile("exploit.sh");
        Files.write(shFile.toPath(), "#!/bin/sh\necho hello".getBytes());

        HtmlPage profilePage = webClient.getPage(baseUrl + "/ta/profile.jsp");
        HtmlFileInput fileInput = profilePage.getHtmlElementById("cvFile");
        fileInput.setFiles(shFile);
        webClient.waitForBackgroundJavaScript(2000);

        HtmlPage result = (HtmlPage) webClient.getCurrentWindow().getEnclosedPage();
        assertTrue(
            "上传 .sh 文件后必须重定向到 ?error=invalid_format",
            result.getUrl().toString().contains("error=invalid_format")
        );
    }


    /**
     * Uploads a valid PDF (with magic bytes) through the profile page and asserts that the
     * servlet redirects to {@code ?uploadStatus=success}.
     *
     * <p>Exercises the happy path (Section 4): a small PDF with correct magic bytes must
     * pass servlet validation and redirect with a success status parameter.</p>
     *
     * @throws Exception if login, page navigation, or file upload simulation fails
     */

    @Test
    public void uploadPdfFile_servletRedirectsToUploadSuccess() throws Exception {
        loginAs(TA_USER, TA_PASSWORD);

        // 创建一个带 PDF magic bytes 的小文件
        File pdfFile = folder.newFile("cv_test.pdf");
        Files.write(pdfFile.toPath(), new byte[]{0x25, 0x50, 0x44, 0x46, 0x2D}); // %PDF-

        HtmlPage profilePage = webClient.getPage(baseUrl + "/ta/profile.jsp");
        HtmlFileInput fileInput = profilePage.getHtmlElementById("cvFile");
        fileInput.setFiles(pdfFile);
        webClient.waitForBackgroundJavaScript(2000);

        HtmlPage result = (HtmlPage) webClient.getCurrentWindow().getEnclosedPage();
        String url = result.getUrl().toString();
        assertTrue(
            "上传合法 PDF 后，Servlet 必须重定向到 ?uploadStatus=success，实际 URL: " + url,
            url.contains("uploadStatus=success")
        );
    }


    /**
     * Uploads a valid DOCX file through the profile page and asserts that the servlet
     * redirects to {@code ?uploadStatus=success}.
     *
     * <p>Exercises the happy path (Section 4) for Word Open XML documents, confirming
     * that {@code .docx} uploads are accepted at the servlet layer.</p>
     *
     * @throws Exception if login, page navigation, or file upload simulation fails
     */

    @Test
    public void uploadDocxFile_servletRedirectsToUploadSuccess() throws Exception {
        loginAs(TA_USER, TA_PASSWORD);

        File docxFile = folder.newFile("resume.docx");
        Files.write(docxFile.toPath(), new byte[1024]); // placeholder bytes

        HtmlPage profilePage = webClient.getPage(baseUrl + "/ta/profile.jsp");
        HtmlFileInput fileInput = profilePage.getHtmlElementById("cvFile");
        fileInput.setFiles(docxFile);
        webClient.waitForBackgroundJavaScript(2000);

        HtmlPage result = (HtmlPage) webClient.getCurrentWindow().getEnclosedPage();
        assertTrue(
            "上传合法 DOCX 后，Servlet 必须重定向到 ?uploadStatus=success",
            result.getUrl().toString().contains("uploadStatus=success")
        );
    }


    /**
     * Accesses the upload endpoint without authentication and asserts that the request is
     * redirected to the login page.
     *
     * <p>Exercises servlet access control (Section 4): unauthenticated requests to
     * {@code /ta/upload-cv} must not reach the upload handler and must redirect to
     * {@code /login}.</p>
     *
     * @throws Exception if page navigation fails
     */

    @Test
    public void uploadWithoutLogin_servletRedirectsToLoginPage() throws Exception {
        // 不登录直接访问上传端点，应重定向到 login
        HtmlPage result = webClient.getPage(baseUrl + "/ta/upload-cv");
        assertTrue(
            "未登录直接访问上传端点必须重定向到登录页",
            result.getUrl().toString().contains("/login")
        );
    }

    // ═══════════════════════════════════════════════════════════════════════

    // Section 5 — UI layer: error/success message rendering on profile.jsp
    //   Loads profile.jsp with query parameters; verifies JSP banner content
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Loads {@code profile.jsp?error=invalid_format} and asserts that an error banner
     * containing {@code "Invalid file format"} is rendered.
     *
     * <p>Exercises UI error rendering (Section 5): the JSP must display a visible error
     * banner when the servlet redirects with the {@code invalid_format} query parameter.</p>
     *
     * @throws Exception if login or page navigation fails
     */

    // Section 5 — UI 层：错误/成功消息在页面上的展示验证
    //   直接带参数访问 profile.jsp，验证 JSP 渲染出正确的提示文案
    // ═══════════════════════════════════════════════════════════════════════


    @Test
    public void profilePage_invalidFormatParam_displaysErrorBanner() throws Exception {
        loginAs(TA_USER, TA_PASSWORD);
        HtmlPage page = webClient.getPage(baseUrl + "/ta/profile.jsp?error=invalid_format");
        String body = page.getBody().getTextContent();
        assertTrue(
            "invalid_format 参数必须触发页面上的错误横幅（含 'Invalid file format'）",
            body.contains("Invalid file format")
        );
    }


    /**
     * Loads {@code profile.jsp?error=invalid_format} and asserts that the error message
     * lists supported formats (PDF, DOC) and no longer mentions removed types (JPG, PNG).
     *
     * <p>Exercises UI error content (Section 5): the invalid-format banner must guide
     * users toward allowed document types and must not reference deprecated image formats.</p>
     *
     * @throws Exception if login or page navigation fails
     */

    @Test
    public void profilePage_invalidFormatParam_mentionsSupportedFormats() throws Exception {
        loginAs(TA_USER, TA_PASSWORD);
        HtmlPage page = webClient.getPage(baseUrl + "/ta/profile.jsp?error=invalid_format");
        String body = page.getBody().getTextContent();
        assertTrue("错误消息必须提及 PDF",  body.contains("PDF"));
        assertTrue("错误消息必须提及 DOC",  body.contains("DOC"));
        assertFalse("错误消息不应再提及 JPG（已从允许格式中移除）", body.contains("JPG"));
        assertFalse("错误消息不应再提及 PNG（已从允许格式中移除）", body.contains("PNG"));
    }


    /**
     * Loads {@code profile.jsp?error=too_large} and asserts that a size-limit error banner
     * is displayed on the page.
     *
     * <p>Exercises UI error rendering (Section 5): the {@code too_large} query parameter
     * must produce a user-visible message referencing the upload size restriction.</p>
     *
     * @throws Exception if login or page navigation fails
     */

    @Test
    public void profilePage_tooLargeParam_displaysErrorBanner() throws Exception {
        loginAs(TA_USER, TA_PASSWORD);
        HtmlPage page = webClient.getPage(baseUrl + "/ta/profile.jsp?error=too_large");
        String body = page.getBody().getTextContent();
        assertTrue(
            "too_large 参数必须触发页面上的错误横幅（含大小限制相关文字）",
            body.contains("10") || body.contains("limit") || body.contains("exceeds")
        );
    }


    /**
     * Loads {@code profile.jsp?uploadStatus=success} and asserts that a success banner
     * is displayed on the page.
     *
     * <p>Exercises UI success rendering (Section 5): after a successful upload redirect,
     * the profile page must show confirmation text such as {@code "Successful"},
     * {@code "uploaded"}, or {@code "updated"}.</p>
     *
     * @throws Exception if login or page navigation fails
     */

    @Test
    public void profilePage_uploadSuccessParam_displaysSuccessBanner() throws Exception {
        loginAs(TA_USER, TA_PASSWORD);
        HtmlPage page = webClient.getPage(baseUrl + "/ta/profile.jsp?uploadStatus=success");
        String body = page.getBody().getTextContent();
        assertTrue(
            "uploadStatus=success 参数必须显示成功横幅",
            body.contains("Successful") || body.contains("uploaded") || body.contains("updated")
        );
    }


    /**
     * Loads {@code profile.jsp?error=invalid_format} and asserts that the error banner HTML
     * includes red styling markers or the {@code "Upload Failed"} label.
     *
     * <p>Exercises UI visual feedback (Section 5): error banners must use distinctive
     * red styling (e.g. {@code fef2f2}, {@code b91c1c}) or an explicit
     * {@code "Upload Failed"} heading.</p>
     *
     * @throws Exception if login or page navigation fails
     */

    @Test
    public void profilePage_uploadFailed_bannerHasRedStyling() throws Exception {
        loginAs(TA_USER, TA_PASSWORD);
        HtmlPage page = webClient.getPage(baseUrl + "/ta/profile.jsp?error=invalid_format");
        // 检查 HTML 源码：错误横幅应使用红色背景样式
        String html = page.getWebResponse().getContentAsString();
        assertTrue(
            "错误横幅必须包含红色背景样式（fef2f2 或 b91c1c）",
            html.contains("fef2f2") || html.contains("b91c1c") || html.contains("Upload Failed")
        );
    }


    /**
     * Loads {@code profile.jsp?uploadStatus=success} and asserts that the success banner HTML
     * includes green styling markers or the {@code "Upload Successful"} label.
     *
     * <p>Exercises UI visual feedback (Section 5): success banners must use distinctive
     * green styling (e.g. {@code f0fdf4}, {@code 15803d}) or an explicit
     * {@code "Upload Successful"} heading.</p>
     *
     * @throws Exception if login or page navigation fails
     */

    @Test
    public void profilePage_uploadSuccess_bannerHasGreenStyling() throws Exception {
        loginAs(TA_USER, TA_PASSWORD);
        HtmlPage page = webClient.getPage(baseUrl + "/ta/profile.jsp?uploadStatus=success");
        String html = page.getWebResponse().getContentAsString();
        assertTrue(
            "成功横幅必须包含绿色背景样式（f0fdf4 或 15803d）",
            html.contains("f0fdf4") || html.contains("15803d") || html.contains("Upload Successful")
        );
    }


    /**
     * Loads {@code profile.jsp} without error query parameters and asserts that no
     * {@code "Upload Failed"} banner is shown.
     *
     * <p>Exercises UI default state (Section 5): when no error query parameter is present,
     * the profile page must not display a failure banner.</p>
     *
     * @throws Exception if login or page navigation fails
     */

    @Test
    public void profilePage_noErrorParam_showsNoErrorBanner() throws Exception {
        loginAs(TA_USER, TA_PASSWORD);
        HtmlPage page = webClient.getPage(baseUrl + "/ta/profile.jsp");
        String body = page.getBody().getTextContent();
        assertFalse("无错误参数时，不应显示 Upload Failed 横幅",
                body.contains("Upload Failed"));
    }


    /**
     * Loads the profile page and asserts that the upload hint text lists only allowed
     * document formats (PDF, DOC) and no longer mentions JPG or PNG.
     *
     * <p>Exercises UI form hints (Section 5): the static help text near the file input
     * must reflect the current allowed-format policy.</p>
     *
     * @throws Exception if login or page navigation fails
     */

    @Test
    public void profilePage_formatHint_onlyMentionsAllowedFormats() throws Exception {
        loginAs(TA_USER, TA_PASSWORD);
        HtmlPage page = webClient.getPage(baseUrl + "/ta/profile.jsp");
        String body = page.getBody().getTextContent();
        // 上传区提示文字应只列合法格式
        assertTrue("提示文字必须包含 PDF",  body.contains("PDF"));
        assertTrue("提示文字必须包含 DOC",  body.contains("DOC"));
        assertFalse("提示文字不应再列出 JPG", body.contains("JPG"));
        assertFalse("提示文字不应再列出 PNG", body.contains("PNG"));
    }


    /**
     * Loads the profile page and asserts that the file input {@code accept} attribute
     * permits {@code .pdf} and {@code .docx} but excludes image types such as
     * {@code .jpg} and {@code .png}.
     *
     * <p>Exercises UI client-side constraints (Section 5): the HTML {@code accept}
     * attribute on the CV file input must restrict the browser file picker to document
     * formats only.</p>
     *
     * @throws Exception if login or page navigation fails
     */

    @Test
    public void profilePage_fileInput_acceptAttributeOnlyAllowsDocFormats() throws Exception {
        loginAs(TA_USER, TA_PASSWORD);
        HtmlPage page = webClient.getPage(baseUrl + "/ta/profile.jsp");
        String html = page.getWebResponse().getContentAsString();
        assertTrue("accept 属性必须包含 .pdf",  html.contains(".pdf"));
        assertTrue("accept 属性必须包含 .docx", html.contains(".docx"));
        assertFalse("accept 属性不应包含 .jpg",  html.contains(".jpg"));
        assertFalse("accept 属性不应包含 .png",  html.contains(".png"));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Helper methods
    // ═══════════════════════════════════════════════════════════════════════

    private HtmlPage loginAs(String username, String password) throws Exception {
        HtmlPage loginPage = webClient.getPage(baseUrl + "/login.jsp");
        HtmlForm form = loginPage.getForms().get(0);
        ((HtmlInput) form.getInputByName("username")).setValueAttribute(username);
        ((HtmlPasswordInput) form.getInputByName("password")).setValueAttribute(password);
        HtmlPage result = ((HtmlButton) form.getFirstByXPath(".//button[@type='submit']")).click();
        webClient.waitForBackgroundJavaScript(1000);
        return result;
    }

    private static void registerTA(String username, String email, String password) throws Exception {
        try (WebClient c = new WebClient(BrowserVersion.CHROME)) {
            c.getOptions().setCssEnabled(false);
            c.getOptions().setJavaScriptEnabled(false);
            c.getOptions().setThrowExceptionOnScriptError(false);
            HtmlPage page = c.getPage(baseUrl + "/register.jsp");
            HtmlForm form = page.getForms().get(0);
            ((HtmlInput)         form.getInputByName("username"))        .setValueAttribute(username);
            ((HtmlEmailInput)    form.getInputByName("email"))           .setValueAttribute(email);
            ((HtmlSelect)        form.getSelectByName("role"))           .getOptionByValue("TA").setSelected(true);
            ((HtmlPasswordInput) form.getInputByName("password"))        .setValueAttribute(password);
            ((HtmlPasswordInput) form.getInputByName("confirmPassword")) .setValueAttribute(password);
            ((HtmlSelect)        form.getSelectByName("securityQuestion")).getOptionByValue("pet").setSelected(true);
            ((HtmlInput)         form.getInputByName("securityAnswer"))  .setValueAttribute("cat");
            ((HtmlButton)        form.getFirstByXPath(".//button[@type='submit']")).click();
        }
    }

    private static void prepareWebappCopy() throws IOException {
        Path source = Paths.get("src/main/webapp");
        Path target = Paths.get("target/cv-upload-webapp");
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

