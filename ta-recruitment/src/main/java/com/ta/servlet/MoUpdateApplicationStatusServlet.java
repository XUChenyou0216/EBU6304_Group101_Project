package com.ta.servlet;

import com.ta.dao.ApplicationDAO;
import com.ta.dao.JobDAO;
import com.ta.dao.NotificationDAO;
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
        if (applicationId == null || newStatus == null) {
            resp.sendRedirect(req.getContextPath() + "/mo/applicants?error=invalid");
            return;
        }

        String dataDir = SessionUtil.getDataDir(req);
        ApplicationDAO appDao = new ApplicationDAO(dataDir);
        Application app = appDao.findById(applicationId);
        if (app == null) {
            resp.sendRedirect(req.getContextPath() + "/mo/applicants?error=notfound");
            return;
        }
        Job job = new JobDAO(dataDir).findById(app.getJobId());
        if (job == null || !job.getMoUserId().equals(u.getUserId())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        app.setStatus(newStatus.trim().toUpperCase());
        String reviewNote = req.getParameter("reviewNote");
        if (reviewNote != null) {
            app.setReviewNote(reviewNote.trim());
        }
        appDao.update(app);

        // Notify TA that their application status has changed
        String statusLabel = newStatus.trim().toUpperCase();
        String taMsg = "Your application for \"" + job.getJobTitle() + "\" (" + job.getModuleCode() + ") has been updated to: " + statusLabel + ".";
        NotificationDAO notifDAO = new NotificationDAO(dataDir);
        notifDAO.save(new Notification(notifDAO.generateNextId(), app.getTaUserId(),
            "STATUS_UPDATED", taMsg, false, LocalDate.now().toString()));

        StringBuilder redir = new StringBuilder(req.getContextPath() + "/mo/applicants?success=updated");
        if (jobIdParam != null && !jobIdParam.isEmpty()) {
            redir.append("&jobId=").append(java.net.URLEncoder.encode(jobIdParam, java.nio.charset.StandardCharsets.UTF_8));
        }
        resp.sendRedirect(redir.toString());
    }
}
