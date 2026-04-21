package com.ta.servlet;

import com.ta.dao.ApplicationDAO;
import com.ta.dao.JobDAO;
import com.ta.dao.NotificationDAO;
import com.ta.dao.TAProfileDAO;
import com.ta.model.Application;
import com.ta.model.Job;
import com.ta.model.Notification;
import com.ta.model.User;
import com.ta.util.SessionUtil;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.time.LocalDate;
import com.ta.util.JobDeadlineUtil;

@WebServlet("/ta/apply")
public class ApplyServlet extends HttpServlet {

    /** GET /ta/apply?jobId=X — show job detail + apply confirmation page */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User currentUser = SessionUtil.getCurrentUser(req);
        String dataDir = SessionUtil.getDataDir(req);
        String jobId = req.getParameter("jobId");

        if (jobId == null || jobId.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/ta/jobs.jsp");
            return;
        }

        Job job = new JobDAO(dataDir).findById(jobId);
        if (job == null || !"OPEN".equals(job.getStatus())) {
            resp.sendRedirect(req.getContextPath() + "/ta/jobs.jsp?error=closed");
            return;
        }

        boolean alreadyApplied = new ApplicationDAO(dataDir)
                .hasApplied(currentUser.getUserId(), jobId);
        boolean hasProfile = new TAProfileDAO(dataDir)
                .findByUserId(currentUser.getUserId()) != null;

        req.setAttribute("job", job);
        req.setAttribute("alreadyApplied", alreadyApplied);
        req.setAttribute("hasProfile", hasProfile);
        req.getRequestDispatcher("/ta/job-detail.jsp").forward(req, resp);
    }

    /** POST /ta/apply — submit application, then redirect */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User currentUser = SessionUtil.getCurrentUser(req);
        String dataDir = SessionUtil.getDataDir(req);
        String jobId = req.getParameter("jobId");

        if (jobId == null || jobId.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/ta/jobs.jsp");
            return;
        }

        // Must have a profile before applying
        if (new TAProfileDAO(dataDir).findByUserId(currentUser.getUserId()) == null) {
            resp.sendRedirect(req.getContextPath() + "/ta/jobs.jsp?error=noprofile");
            return;
        }

        // Job must still be open
        Job job = new JobDAO(dataDir).findById(jobId);
        if (job == null || !"OPEN".equals(job.getStatus())) {
            resp.sendRedirect(req.getContextPath() + "/ta/jobs.jsp?error=closed");
            return;
        }

        ApplicationDAO appDAO = new ApplicationDAO(dataDir);

        // No duplicate applications
        if (appDAO.hasApplied(currentUser.getUserId(), jobId)) {
            resp.sendRedirect(req.getContextPath() + "/ta/jobs.jsp?error=duplicate");
            return;
        }

        Application app = new Application(
                appDAO.generateNextId(),
                currentUser.getUserId(),
                jobId,
                "SUBMITTED",
                LocalDate.now().toString(),
                ""
        );
        appDAO.save(app);

        // Notify TA: application submitted
        String today = LocalDate.now().toString();
        NotificationDAO notifDAO = new NotificationDAO(dataDir);
        String taMsg = "Your application for \"" + job.getJobTitle() + "\" (" + job.getModuleCode() + ") has been submitted successfully.";
        notifDAO.save(new Notification(notifDAO.generateNextId(), currentUser.getUserId(),
            "APPLICATION_SUBMITTED", taMsg, false, today));

        // Notify MO: new application received
        String moMsg = "A new application has been received for \"" + job.getJobTitle() + "\" (" + job.getModuleCode() + ").";
        notifDAO.save(new Notification(notifDAO.generateNextId(), job.getMoUserId(),
            "APPLICATION_SUBMITTED", moMsg, false, today));

        resp.sendRedirect(req.getContextPath() + "/ta/applications.jsp?success=applied");
    }
}
