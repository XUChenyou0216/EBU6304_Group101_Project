package com.ta.servlet;

import com.ta.dao.ApplicationDAO;
import com.ta.dao.JobDAO;
import com.ta.model.Application;
import com.ta.model.Job;
import com.ta.model.User;
import com.ta.util.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Saves all status dropdowns from the Applicants & Review page in one request.
 * POST parameter {@code batchPayload}: {@code APP001,ACCEPTED;APP002,UNDER_REVIEW;...}
 */
@WebServlet("/mo/batch-update-applications")
public class MoBatchUpdateApplicationStatusServlet extends HttpServlet {

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
            resp.sendRedirect(req.getContextPath() + "/mo/applicants?error=batch");
            return;
        }

        String dataDir = SessionUtil.getDataDir(req);
        ApplicationDAO appDao = new ApplicationDAO(dataDir);
        JobDAO jobDao = new JobDAO(dataDir);
        boolean skippedForCapacity = false;

        for (String part : payload.split(";")) {
            part = part.trim();
            if (part.isEmpty()) continue;
            int c = part.indexOf(',');
            if (c <= 0) continue;
            String applicationId = part.substring(0, c).trim();
            String newStatus = part.substring(c + 1).trim();
            Application app = appDao.findById(applicationId);
            if (app == null) continue;
            Job job = jobDao.findById(app.getJobId());
            if (job == null || !job.getMoUserId().equals(u.getUserId())) continue;
            String normalized = newStatus.toUpperCase();
            if (appDao.isAcceptanceCapExceeded(job, app, normalized)) {
                skippedForCapacity = true;
                continue;
            }
            app.setStatus(normalized);
            appDao.update(app);
        }

        StringBuilder redir = new StringBuilder(req.getContextPath() + "/mo/applicants?success=batch");
        if (skippedForCapacity) {
            redir.append("&warning=capacity");
        }
        if (jobIdParam != null && !jobIdParam.isEmpty()) {
            redir.append("&jobId=").append(java.net.URLEncoder.encode(jobIdParam, java.nio.charset.StandardCharsets.UTF_8));
        }
        resp.sendRedirect(redir.toString());
    }
}
