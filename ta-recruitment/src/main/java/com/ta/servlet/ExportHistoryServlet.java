package com.ta.servlet;

import com.ta.model.User;
import com.ta.util.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * GET /admin/exportHistory
 *
 * Exports the complete TA application history as a human-readable .txt archive
 * suitable for auditing by other departments.
 *
 * Data sources (from webappdata/):
 *   applications.csv : applicationId, taUserId, jobId, status, appliedDate, reviewNote
 *   jobs.csv         : jobId, moUserId, moduleName, description, requirements,
 *                      vacancies, deadline, status, createdDate
 *
 * Each record is printed as a clearly separated block containing:
 *   - Applicant details  (userId, role)
 *   - Module / job info  (module name, MO, vacancies, deadline)
 *   - Application status and date
 *   - Reviewer feedback / notes
 */
@WebServlet(urlPatterns = {"/admin/exportHistory"})
public class ExportHistoryServlet extends HttpServlet {

    private static final String SEP =
            "========================================\n";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ── 1. Auth guard ──────────────────────────────────────────────────
        User currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null || !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin access required.");
            return;
        }

        // ── 2. Resolve data directory ──────────────────────────────────────
        String dataDir = SessionUtil.getDataDir(request);
        Path appsPath = Paths.get(dataDir, "applications.csv");
        Path jobsPath = Paths.get(dataDir, "jobs.csv");

        // ── 3. Load jobs into a map: jobId → String[] ──────────────────────
        // header: jobId,moUserId,moduleName,description,requirements,vacancies,deadline,status,createdDate
        Map<String, String[]> jobMap = new LinkedHashMap<>();
        if (Files.exists(jobsPath)) {
            List<String> lines = Files.readAllLines(jobsPath, StandardCharsets.UTF_8);
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;
                String[] cols = parseCsvLine(line);
                if (cols.length >= 1) {
                    jobMap.put(cols[0].trim(), cols);
                }
            }
        }

        // ── 4. Build TXT archive ───────────────────────────────────────────
        StringBuilder sb = new StringBuilder();

        // File-level header
        sb.append("TA RECRUITMENT SYSTEM - APPLICATION HISTORY ARCHIVE\n");
        sb.append("Export Date    : ").append(nowDateTime()).append("\n");
        sb.append("Exported By    : Administrator\n");
        sb.append("Purpose        : Full audit record of all TA applications\n");
        sb.append(SEP).append("\n");

        int total = 0;

        if (Files.exists(appsPath)) {
            List<String> lines = Files.readAllLines(appsPath, StandardCharsets.UTF_8);
            // header: applicationId,taUserId,jobId,status,appliedDate,reviewNote
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;

                String[] app = parseCsvLine(line);
                String applicationId = get(app, 0);
                String taUserId      = get(app, 1);
                String jobId         = get(app, 2);
                String appStatus     = get(app, 3);
                String appliedDate   = get(app, 4);
                String reviewNote    = get(app, 5);

                // Normalise empty review note
                if (reviewNote.isEmpty() || reviewNote.equals("\"\"")) {
                    reviewNote = "(no feedback provided)";
                }

                // Join with job data
                String[] job         = jobMap.get(jobId);
                String moduleName    = job != null ? get(job, 2) : "N/A";
                String moUserId      = job != null ? get(job, 1) : "N/A";
                String description   = job != null ? get(job, 3) : "N/A";
                String requirements  = job != null ? get(job, 4) : "N/A";
                String vacancies     = job != null ? get(job, 5) : "N/A";
                String deadline      = job != null ? get(job, 6) : "N/A";
                String jobStatus     = job != null ? get(job, 7) : "N/A";

                total++;

                sb.append(SEP);
                sb.append(String.format("Application ID   : %s%n", applicationId));
                sb.append("\n");

                sb.append("  [Applicant Details]\n");
                sb.append(String.format("  Applicant User ID  : %s%n", taUserId));
                sb.append(String.format("  Role               : Teaching Assistant (TA)%n"));
                sb.append("\n");

                sb.append("  [Job / Module Details]\n");
                sb.append(String.format("  Job ID             : %s%n", jobId));
                sb.append(String.format("  Module Name        : %s%n", moduleName));
                sb.append(String.format("  Module Organiser   : %s%n", moUserId));
                sb.append(String.format("  Description        : %s%n", description));
                sb.append(String.format("  Requirements       : %s%n", requirements));
                sb.append(String.format("  Vacancies          : %s%n", vacancies));
                sb.append(String.format("  Application Deadline: %s%n", deadline));
                sb.append(String.format("  Job Status         : %s%n", jobStatus));
                sb.append("\n");

                sb.append("  [Application Record]\n");
                sb.append(String.format("  Application Status : %s%n", appStatus));
                sb.append(String.format("  Applied Date       : %s%n", appliedDate));
                sb.append("\n");

                sb.append("  [Reviewer Feedback]\n");
                sb.append(String.format("  Review Note        : %s%n", reviewNote));
                sb.append("\n");
            }
        }

        // File footer
        sb.append(SEP);
        sb.append(String.format("Total Records    : %d%n", total));
        sb.append("End of Archive\n");

        // ── 5. Stream to browser as file download ──────────────────────────
        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        String filename = "TA_History_Archive_" + today() + ".txt";

        response.setContentType("text/plain; charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + filename + "\"");
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private static String get(String[] arr, int idx) {
        if (arr == null || idx >= arr.length) return "";
        String v = arr[idx];
        return (v == null) ? "" : v.trim();
    }

    private static String today() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    private static String nowDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    private static String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        result.add(cur.toString());
        return result.toArray(new String[0]);
    }
}
