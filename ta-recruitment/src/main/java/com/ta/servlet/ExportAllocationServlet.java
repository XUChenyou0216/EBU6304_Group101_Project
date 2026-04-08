package com.ta.servlet;

import com.ta.util.SessionUtil;
import com.ta.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * GET /admin/exportAllocation
 *
 * Exports TA final allocation as a downloadable CSV file.
 *
 * Data sources (from webappdata/):
 *   applications.csv : applicationId, taUserId, jobId, status, appliedDate, reviewNote
 *   jobs.csv         : jobId, moUserId, moduleName, description, requirements,
 *                      vacancies, deadline, status, createdDate
 *
 * Output columns:
 *   ApplicationId, ApplicantUserId, JobId, ModuleName, ModuleOrganiserId,
 *   Vacancies, Deadline, ApplicationStatus, AppliedDate, ReviewNote
 */
@WebServlet(urlPatterns = {"/admin/exportAllocation"})
public class ExportAllocationServlet extends HttpServlet {

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

        // ── 4. Build CSV ───────────────────────────────────────────────────
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF'); // UTF-8 BOM — ensures Excel opens correctly
        sb.append("ApplicationId,ApplicantUserId,JobId,ModuleName,ModuleOrganiserId,"
                + "Vacancies,Deadline,ApplicationStatus,AppliedDate,ReviewNote\r\n");

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

                // 只导出 Accepted 的记录
                if (!"ACCEPTED".equalsIgnoreCase(appStatus)) continue;
                String appliedDate   = get(app, 4);
                String reviewNote    = get(app, 5);

                // Join with job data
                String[] job = jobMap.get(jobId);
                String moduleName = job != null ? get(job, 2) : "";
                String moUserId   = job != null ? get(job, 1) : "";
                String vacancies  = job != null ? get(job, 5) : "";
                String deadline   = job != null ? get(job, 6) : "";

                sb.append(cell(applicationId)).append(',')
                  .append(cell(taUserId))     .append(',')
                  .append(cell(jobId))        .append(',')
                  .append(cell(moduleName))   .append(',')
                  .append(cell(moUserId))     .append(',')
                  .append(cell(vacancies))    .append(',')
                  .append(cell(deadline))     .append(',')
                  .append(cell(appStatus))    .append(',')
                  .append(cell(appliedDate))  .append(',')
                  .append(cell(reviewNote))   .append("\r\n");
            }
        }

        // ── 5. Stream to browser as file download ──────────────────────────
        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        String filename = "TA_Final_Allocation_" + today() + ".csv";

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + filename + "\"");
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    /** Wrap field in double-quotes if it contains commas, quotes, or newlines. */
    private static String cell(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /** Safe indexed getter with null/bounds safety. */
    private static String get(String[] arr, int idx) {
        if (arr == null || idx >= arr.length) return "";
        String v = arr[idx];
        return (v == null) ? "" : v.trim();
    }

    /** Today's date formatted as YYYY-MM-DD. */
    private static String today() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    /**
     * CSV line parser — handles double-quoted fields that may contain commas.
     */
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
