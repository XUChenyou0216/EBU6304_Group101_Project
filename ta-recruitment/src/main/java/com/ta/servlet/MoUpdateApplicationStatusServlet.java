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
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

/**
 * MO updates a single application status (Applicants & Review table).
 */
@WebServlet("/mo/update-status")
public class MoUpdateApplicationStatusServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User u = SessionUtil.getCurrentUser(req);
        if (u == null || !"MO".equalsIgnoreCase(u.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String applicationId = req.getParameter("applicationId");
        String newStatus = req.getParameter("newStatus");
        String jobIdParam = req.getParameter("jobId");
        String returnTo = req.getParameter("returnTo");
        if (applicationId == null || newStatus == null) {
            if ("progress".equals(returnTo)) {
                resp.sendRedirect(req.getContextPath() + "/mo/progress.jsp?error=invalid");
            } else if ("all".equals(returnTo)) {
                resp.sendRedirect(req.getContextPath() + "/mo/applicants?error=invalid");
            } else {
                resp.sendRedirect(req.getContextPath() + "/mo/applicants?error=invalid");
            }
            return;
        }

        String dataDir = SessionUtil.getDataDir(req);
        ApplicationDAO appDao = new ApplicationDAO(dataDir);
        Application app = appDao.findById(applicationId);
        if (app == null) {
            if ("progress".equals(returnTo)) {
                resp.sendRedirect(req.getContextPath() + "/mo/progress.jsp?error=notfound");
            } else if ("all".equals(returnTo)) {
                resp.sendRedirect(req.getContextPath() + "/mo/applicants?error=notfound");
            } else {
                resp.sendRedirect(req.getContextPath() + "/mo/applicants?error=notfound");
            }
            return;
        }
        Job job = new JobDAO(dataDir).findById(app.getJobId());
        if (job == null || !job.getMoUserId().equals(u.getUserId())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String normalized = newStatus.trim().toUpperCase();
        if (appDao.isAcceptanceCapExceeded(job, app, normalized)) {
            if ("progress".equals(returnTo)) {
                resp.sendRedirect(req.getContextPath() + "/mo/progress.jsp?error=capacity");
            } else if ("all".equals(returnTo)) {
                resp.sendRedirect(req.getContextPath() + "/mo/applicants?error=capacity");
            } else {
                StringBuilder capRedir = new StringBuilder(req.getContextPath() + "/mo/applicants?error=capacity");
                if (jobIdParam != null && !jobIdParam.isEmpty()) {
                    capRedir.append("&jobId=").append(URLEncoder.encode(jobIdParam, StandardCharsets.UTF_8));
                }
                resp.sendRedirect(capRedir.toString());
            }
            return;
        }

        app.setStatus(normalized);
        String reviewNote = req.getParameter("reviewNote");
        if (reviewNote != null) {
            app.setReviewNote(reviewNote.trim());
        }
        appDao.update(app);

        // Notify TA that their application status has changed
        String statusLabel = normalized;
        String taMsg = "Your application for \"" + job.getJobTitle() + "\" (" + job.getModuleCode() + ") has been updated to: " + statusLabel + ".";
        NotificationDAO notifDAO = new NotificationDAO(dataDir);
        notifDAO.save(new Notification(notifDAO.generateNextId(), app.getTaUserId(),
            "STATUS_UPDATED", taMsg, false, LocalDate.now().toString()));

        // If this acceptance pushes the TA over the workload limit, alert all admins
        if ("ACCEPTED".equals(normalized)) {
            long acceptedCount = appDao.findByTa(app.getTaUserId()).stream()
                    .filter(a -> "ACCEPTED".equals(a.getStatus()))
                    .count();
            int estimatedHours = (int) (acceptedCount
                    * AdminWorkloadServlet.HOURS_PER_ACCEPTED_ASSIGNMENT);
            if (estimatedHours > AdminWorkloadServlet.WORKLOAD_LIMIT_HOURS) {
                UserDAO userDAO = new UserDAO(dataDir);
                User ta = userDAO.findById(app.getTaUserId());
                String taName = (ta != null) ? ta.getUsername() : app.getTaUserId();
                String taMarker = "[TA:" + app.getTaUserId() + "]";
                for (User admin : userDAO.findAll()) {
                    if (!"ADMIN".equalsIgnoreCase(admin.getRole())) continue;
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
                            LocalDate.now().toString()
                    ));
                }
            }
        }

        if ("progress".equals(returnTo)) {
            resp.sendRedirect(req.getContextPath() + "/mo/progress.jsp?success=updated");
            return;
        }
        if ("all".equals(returnTo)) {
            resp.sendRedirect(req.getContextPath() + "/mo/applicants?success=updated");
            return;
        }
        StringBuilder redir = new StringBuilder(req.getContextPath() + "/mo/applicants?success=updated");
        if (jobIdParam != null && !jobIdParam.isEmpty()) {
            redir.append("&jobId=").append(URLEncoder.encode(jobIdParam, StandardCharsets.UTF_8));
        }
        resp.sendRedirect(redir.toString());
    }
}
