package com.ta;

import org.htmlunit.BrowserVersion;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlAnchor;
import org.htmlunit.html.HtmlButton;
import org.htmlunit.html.HtmlDivision;
import org.htmlunit.html.HtmlEmailInput;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlInput;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlSelect;
import org.htmlunit.html.HtmlPasswordInput;
import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class SystemEndToEndTest {
    private static Tomcat tomcat;
    private static String baseUrl;
    private WebClient webClient;
    private static File webappRoot;

    @BeforeClass
    public static void startEmbeddedServer() throws Exception {
        prepareWebappCopy();

        tomcat = new Tomcat();
        tomcat.setPort(0);
        tomcat.setBaseDir("target/system-tomcat");

        Context context = tomcat.addWebapp("", webappRoot.getAbsolutePath());
        context.setParentClassLoader(Thread.currentThread().getContextClassLoader());

        WebResourceRoot resources = new StandardRoot(context);
        resources.addPreResources(new DirResourceSet(resources,
                "/WEB-INF/classes",
                new File("target/classes").getAbsolutePath(),
                "/"));
        context.setResources(resources);

        tomcat.start();
        int port = tomcat.getConnector().getLocalPort();
        baseUrl = "http://localhost:" + port;
    }

    @AfterClass
    public static void stopEmbeddedServer() throws Exception {
        if (tomcat != null) {
            tomcat.stop();
            tomcat.destroy();
        }
    }

    @Before
    public void setUp() {
        webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setRedirectEnabled(true);
    }

    @After
    public void tearDown() {
        if (webClient != null) {
            webClient.close();
        }
    }

    @Test
    public void testSystemEndToEndFlow() throws Exception {
        String taUsername = "system_ta_" + System.currentTimeMillis();
        String moUsername = "system_mo_" + System.currentTimeMillis();
        String password = "Pass1234";

        registerUser(taUsername, taUsername + "@example.com", "TA", password);
        registerUser(moUsername, moUsername + "@example.com", "MO", password);

        String jobTitle = "System Test Job " + System.currentTimeMillis();
        login(moUsername, password);
        HtmlPage postJobPage = webClient.getPage(baseUrl + "/mo/post-job");
        assertTrue(postJobPage.getBody().getTextContent().contains("Post New TA Job"));
        HtmlPage postJobResult = publishJob(postJobPage, jobTitle);
        assertTrue(postJobResult.getUrl().toString().contains("/mo/jobs.jsp?success=posted"));
        assertTrue(postJobResult.getBody().getTextContent().contains(jobTitle));

        login(taUsername, password);
        HtmlPage profilePage = webClient.getPage(baseUrl + "/ta/profile.jsp");
        assertTrue(profilePage.getBody().getTextContent().contains("Profile & CV Upload"));
        HtmlPage profileResult = saveTaProfile(profilePage);
        assertTrue(profileResult.getUrl().toString().contains("/ta/profile.jsp?success=true"));

        HtmlPage jobsPage = webClient.getPage(baseUrl + "/ta/jobs.jsp");
        assertTrue(jobsPage.getBody().getTextContent().contains("Available TA Positions"));

        HtmlPage jobDetail = openJobByTitle(jobsPage, jobTitle);
        assertNotNull(jobDetail);
        assertTrue(jobDetail.getBody().getTextContent().contains(jobTitle));
        assertTrue(jobDetail.getBody().getTextContent().contains("Apply for this Position"));

        HtmlPage applicationResult = submitApplication(jobDetail);
        assertTrue(applicationResult.getUrl().toString().contains("/ta/applications.jsp?success=applied"));
        assertTrue(applicationResult.getBody().getTextContent().contains("My Applications") || applicationResult.getBody().getTextContent().contains("Application"));
    }

    private static void prepareWebappCopy() throws IOException {
        Path source = Paths.get("src/main/webapp");
        Path target = Paths.get("target/system-test-webapp");
        if (Files.exists(target)) {
            deleteRecursively(target);
        }
        Files.createDirectories(target);
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path targetDir = target.resolve(source.relativize(dir));
                Files.createDirectories(targetDir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path targetFile = target.resolve(source.relativize(file));
                Files.copy(file, targetFile);
                return FileVisitResult.CONTINUE;
            }
        });
        Files.createDirectories(target.resolve("uploads"));
        webappRoot = target.toFile();
    }

    private HtmlPage registerUser(String username, String email, String role, String password) throws Exception {
        HtmlPage registerPage = webClient.getPage(baseUrl + "/register.jsp");
        HtmlForm form = registerPage.getForms().get(0);
        ((HtmlInput) form.getInputByName("username")).setValueAttribute(username);
        ((HtmlEmailInput) form.getInputByName("email")).setValueAttribute(email);
        HtmlSelect roleSelect = form.getSelectByName("role");
        roleSelect.getOptionByValue(role).setSelected(true);
        ((HtmlPasswordInput) form.getInputByName("password")).setValueAttribute(password);
        ((HtmlPasswordInput) form.getInputByName("confirmPassword")).setValueAttribute(password);
        ((HtmlSelect) form.getSelectByName("securityQuestion")).getOptionByValue("pet").setSelected(true);
        ((HtmlInput) form.getInputByName("securityAnswer")).setValueAttribute("buddy");
        HtmlButton submitButton = form.getFirstByXPath(".//button[@type='submit']");
        HtmlPage result = submitButton.click();
        webClient.waitForBackgroundJavaScript(1000);
        assertTrue("Register redirect must end on login page", result.getUrl().toString().contains("/login"));
        return result;
    }

    private HtmlPage login(String username, String password) throws Exception {
        HtmlPage loginPage = webClient.getPage(baseUrl + "/login.jsp");
        HtmlForm loginForm = loginPage.getForms().get(0);
        ((HtmlInput) loginForm.getInputByName("username")).setValueAttribute(username);
        ((HtmlPasswordInput) loginForm.getInputByName("password")).setValueAttribute(password);
        HtmlButton submitButton = loginForm.getFirstByXPath(".//button[@type='submit']");
        HtmlPage dashboard = submitButton.click();
        webClient.waitForBackgroundJavaScript(1000);
        assertTrue("Login should redirect to dashboard", dashboard.getUrl().toString().contains("/dashboard.jsp"));
        return dashboard;
    }

    private HtmlPage publishJob(HtmlPage page, String jobTitle) throws Exception {
        HtmlForm jobForm = page.getForms().get(0);
        ((HtmlInput) jobForm.getInputByName("moduleCode")).setValueAttribute("CS999");
        ((HtmlInput) jobForm.getInputByName("moduleName")).setValueAttribute("System Test Module");
        ((HtmlInput) jobForm.getInputByName("jobTitle")).setValueAttribute(jobTitle);
        jobForm.getTextAreaByName("description").setText("End-to-end system test job description.");
        ((HtmlInput) jobForm.getInputByName("vacancies")).setValueAttribute("1");
        ((HtmlInput) jobForm.getInputByName("deadline")).setValueAttribute(LocalDate.now().plusDays(30).format(DateTimeFormatter.ISO_DATE));
        ((HtmlInput) jobForm.getInputByName("workingPeriod")).setValueAttribute("Summer 2026");
        jobForm.getTextAreaByName("keyDuties").setText("Assist with lectures and grading.");
        jobForm.getTextAreaByName("requiredSkills").setText("Java, teamwork");
        jobForm.getTextAreaByName("eligibility").setText("Year 3 or above");
        HtmlButton submitButton = jobForm.getFirstByXPath(".//button[@type='submit']");
        HtmlPage result = submitButton.click();
        webClient.waitForBackgroundJavaScript(1000);
        return result;
    }

    private HtmlPage saveTaProfile(HtmlPage page) throws Exception {
        HtmlForm profileForm = page.getForms().get(0);
        ((HtmlInput) profileForm.getInputByName("studentId")).setValueAttribute("202600001");
        ((HtmlInput) profileForm.getInputByName("fullName")).setValueAttribute("System TA");
        ((HtmlInput) profileForm.getInputByName("programme")).setValueAttribute("Computer Science");
        HtmlSelect yearSelect = profileForm.getSelectByName("yearOfStudy");
        yearSelect.getOptionByValue("Year 3").setSelected(true);
        HtmlButton submitButton = profileForm.getFirstByXPath(".//button[@type='submit']");
        HtmlPage result = submitButton.click();
        webClient.waitForBackgroundJavaScript(1000);
        return result;
    }

    private HtmlPage openJobByTitle(HtmlPage page, String title) throws IOException {
        List<?> cards = page.getByXPath("//div[contains(@class,'job-card')]");
        for (Object card : cards) {
            if (card instanceof HtmlDivision) {
                HtmlDivision division = (HtmlDivision) card;
                if (division.getTextContent().contains(title)) {
                    HtmlAnchor detailLink = division.getFirstByXPath(".//a[contains(@href,'/ta/apply?jobId=')]");
                    if (detailLink != null) {
                        HtmlPage details = detailLink.click();
                        webClient.waitForBackgroundJavaScript(1000);
                        return details;
                    }
                }
            }
        }
        return null;
    }

    private HtmlPage submitApplication(HtmlPage jobDetailPage) throws IOException {
        HtmlForm applyForm = jobDetailPage.getForms().get(0);
        HtmlButton submitButton = applyForm.getFirstByXPath(".//button[@type='submit']");
        HtmlPage result = submitButton.click();
        webClient.waitForBackgroundJavaScript(1000);
        return result;
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
