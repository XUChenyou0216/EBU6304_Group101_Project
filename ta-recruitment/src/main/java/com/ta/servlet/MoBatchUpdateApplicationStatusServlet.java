package com.ta.servlet;

import com.ta.dao.ApplicationDAO;
import com.ta.dao.JobDAO;
import com.ta.dao.NotificationDAO;
import com.ta.dao.UserDAO;
import com.ta.model.Application;
import com.ta.model.Job;
import com.ta.model.Notification;
import com.ta.model.User;
import com.ta.util.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Servlet for batch updating application statuses from the MO applicants review page.
 * <p>
 * URL mapping: {@code /mo/batch-update-applications} (declared in {@code WEB-INF/web.xml}).
 * </p>
 * <p>
 * POST parameter {@code batchPayload}: semicolon-separated entries such as
 * {@code APP001,ACCEPTED;APP002,UNDER_REVIEW;...}.
 * Optional {@code includeNotes=true} with {@code note_&lt;applicationId&gt;} per row in the payload.
 * Sends TA status notifications and admin workload alerts when applicable.
 * </p>
 */
public class MoBatchUpdateApplicationStatusServlet extends HttpServlet {

    /**
     * Applies status (and optional review note) updates for all operations in the batch payload.
     *
     * @param req  the HTTP POST request with {@code batchPayload}, optional {@code jobId},
     *             {@code includeNotes}, and per-application {@code note_<applicationId>} fields
     * @param resp the HTTP response; redirects to applicants page with success, error, or capacity warning
     * @throws ServletException if servlet processing fails
     * @throws IOException      if redirecting or sending forbidden fails
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User u = SessionUtil.getCurrentUser(req);
        if (u == null || !"MO".equalsIgnoreCase(u.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String payload = req.getParameter("batchPayload");
        String jobIdParam = req.getParameter("jobId");
        if (payload == null || payload.trim().isEmpty()) {
            resp.sendRedirect(buildApplicantsRedirect(req, jobIdParam, false, false));
            return;
        }

        boolean mergeNotes = "true".equalsIgnoreCase(req.getParameter("includeNotes"));

        List<BatchOp> ops = parsePayload(payload);
        if (ops.isEmpty()) {
            resp.sendRedirect(buildApplicantsRedirect(req, jobIdParam, false, false));
            return;
        }

        String dataDir = SessionUtil.getDataDir(req);
        ApplicationDAO appDao = new ApplicationDAO(dataDir);
        JobDAO jobDao = new JobDAO(dataDir);
        List<Application> all = appDao.findAll();
        Map<String, Application> byId = new HashMap<>(all.size());
        for (Application a : all) {
            byId.put(a.getApplicationId(), a);
        }

        Map<String, Long> acceptedByJob = new HashMap<>();
        for (Application a : all) {
            if (a.getJobId() != null && "ACCEPTED".equals(a.getStatus())) {
                acceptedByJob.merge(a.getJobId(), 1L, Long::sum);
            }
        }

        Map<String, Job> jobById = new HashMap<>();
        for (Job j : jobDao.findAll()) {
            jobById.put(j.getJobId(), j);
        }

        boolean skippedForCapacity = false;
        boolean anyChanged = false;
        List<StatusNotify> pendingNotify = new ArrayList<>();

        for (BatchOp op : ops) {
            Application app = byId.get(op.applicationId);
            if (app == null) {
                continue;
            }
            Job job = jobById.get(app.getJobId());
            if (job == null || !job.getMoUserId().equals(u.getUserId())) {
                continue;
            }
            String normalized = op.status.toUpperCase();
            String oldStatus = app.getStatus() != null ? app.getStatus() : "";

            if (!"ACCEPTED".equals(oldStatus) && "ACCEPTED".equals(normalized)) {
                int cap = Math.max(0, job.getVacancies());
                long cur = acceptedByJob.getOrDefault(app.getJobId(), 0L);
                if (cur >= cap) {
                    skippedForCapacity = true;
                    continue;
                }
            }

            if (!normalized.equals(oldStatus)) {
                if ("ACCEPTED".equals(oldStatus) && !"ACCEPTED".equals(normalized)) {
                    acceptedByJob.merge(app.getJobId(), -1L, Long::sum);
                } else if (!"ACCEPTED".equals(oldStatus) && "ACCEPTED".equals(normalized)) {
                    acceptedByJob.merge(app.getJobId(), 1L, Long::sum);
                }
                app.setStatus(normalized);
                anyChanged = true;
                pendingNotify.add(new StatusNotify(app.getTaUserId(), job.getJobTitle(),
                    job.getModuleCode(), normalized));
            }

            if (mergeNotes) {
                String noteParam = req.getParameter("note_" + op.applicationId);
                if (noteParam != null) {
                    app.setReviewNote(noteParam.trim());
                    anyChanged = true;
                }
            }
        }

        if (anyChanged) {
            appDao.persistAll(all);
            NotificationDAO notifDAO = new NotificationDAO(dataDir);
            String today = LocalDate.now().toString();

            // Notify each TA of their status change
            for (StatusNotify sn : pendingNotify) {
                String taMsg = "Your application for \"" + sn.jobTitle + "\" (" + sn.moduleCode
                    + ") has been updated to: " + sn.status + ".";
                notifDAO.save(new Notification(notifDAO.generateNextId(), sn.taUserId,
                    "STATUS_UPDATED", taMsg, false, today));
            }

            // Check workload limit for every TA that gained an ACCEPTED status in this batch
            Set<String> acceptedTaIds = new HashSet<>();
            for (StatusNotify sn : pendingNotify) {
                if ("ACCEPTED".equals(sn.status)) {
                    acceptedTaIds.add(sn.taUserId);
                }
            }
            if (!acceptedTaIds.isEmpty()) {
                UserDAO userDAO = new UserDAO(dataDir);
                List<User> admins = new ArrayList<>();
                for (User admin : userDAO.findAll()) {
                    if ("ADMIN".equalsIgnoreCase(admin.getRole())) admins.add(admin);
                }
                for (String taId : acceptedTaIds) {
                    long acceptedCount = all.stream()
                            .filter(a -> taId.equals(a.getTaUserId())
                                    && "ACCEPTED".equals(a.getStatus()))
                            .count();
                    int estimatedHours = (int) (acceptedCount
                            * AdminWorkloadServlet.HOURS_PER_ACCEPTED_ASSIGNMENT);
                    if (estimatedHours <= AdminWorkloadServlet.WORKLOAD_LIMIT_HOURS) continue;

                    User ta = userDAO.findById(taId);
                    String taName   = (ta != null) ? ta.getUsername() : taId;
                    String taMarker = "[TA:" + taId + "]";
                    for (User admin : admins) {
                        // Dedup: skip if an unread alert for this TA already exists
                        boolean alreadyPending = notifDAO.findByUser(admin.getUserId()).stream()
                                .anyMatch(n -> Notification.TYPE_WORKLOAD_ALERT.equals(n.getType())
                                        && !n.isRead()
                                        && n.getMessage().contains(taMarker));
                        if (alreadyPending) continue;
                        notifDAO.save(new Notification(
                                notifDAO.generateNextId(),
                                admin.getUserId(),
                                Notification.TYPE_WORKLOAD_ALERT,
                                "Workload alert " + taMarker + ": " + taName
                                        + " has " + estimatedHours + "h"
                                        + " (limit " + AdminWorkloadServlet.WORKLOAD_LIMIT_HOURS + "h).",
                                false,
                                today
                        ));
                    }
                }
            }
        }

        resp.sendRedirect(buildApplicantsRedirect(req, jobIdParam, true, skippedForCapacity));
    }

    private static String buildApplicantsRedirect(HttpServletRequest req, String jobIdParam,
                                                  boolean ok, boolean warnCapacity) {
        StringBuilder redir = new StringBuilder(req.getContextPath()).append("/mo/applicants?");
        if (!ok) {
            redir.append("error=batch");
        } else {
            redir.append("success=batch");
            if (warnCapacity) {
                redir.append("&warning=capacity");
            }
        }
        if (jobIdParam != null && !jobIdParam.isEmpty()) {
            redir.append("&jobId=").append(URLEncoder.encode(jobIdParam, StandardCharsets.UTF_8));
        }
        return redir.toString();
    }

    private static List<BatchOp> parsePayload(String payload) {
        List<BatchOp> out = new ArrayList<>();
        for (String part : payload.split(";")) {
            part = part.trim();
            if (part.isEmpty()) {
                continue;
            }
            int c = part.indexOf(',');
            if (c <= 0 || c >= part.length() - 1) {
                continue;
            }
            String applicationId = part.substring(0, c).trim();
            String newStatus = part.substring(c + 1).trim();
            if (applicationId.isEmpty() || newStatus.isEmpty()) {
                continue;
            }
            out.add(new BatchOp(applicationId, newStatus));
        }
        return out;
    }

    private static final class BatchOp {
        final String applicationId;
        final String status;

        BatchOp(String applicationId, String status) {
            this.applicationId = applicationId;
            this.status = status;
        }
    }

    private static final class StatusNotify {
        final String taUserId;
        final String jobTitle;
        final String moduleCode;
        final String status;

        StatusNotify(String taUserId, String jobTitle, String moduleCode, String status) {
            this.taUserId = taUserId;
            this.jobTitle = jobTitle;
            this.moduleCode = moduleCode;
            this.status = status;
        }
    }
}
