package com.ta.servlet;

import com.ta.dao.ApplicationDAO;
import com.ta.dao.JobDAO;
import com.ta.model.Application;
import com.ta.model.Job;
import com.ta.model.User;
import com.ta.util.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet that closes all OPEN jobs owned by the current MO that have reached acceptance capacity.
 * <p>
 * URL mapping: {@code /mo/close-full-jobs} (declared in {@code WEB-INF/web.xml}).
 * One-click housekeeping from the MO jobs page.
 * </p>
 */
public class MoCloseFullJobsServlet extends HttpServlet {

    /**
     * Sets each full OPEN job owned by the MO to {@code CLOSED} and redirects with a count.
     *
     * @param req  the HTTP POST request from the MO jobs UI
     * @param resp the HTTP response; redirects to {@code /mo/jobs.jsp?success=closedFull&n=<count>}
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
        String dataDir = SessionUtil.getDataDir(req);
        JobDAO jobDAO = new JobDAO(dataDir);
        ApplicationDAO appDao = new ApplicationDAO(dataDir);

        List<Job> allJobs = jobDAO.findAll();
        List<Application> apps = appDao.findAll();
        Map<String, Long> acceptedByJob = new HashMap<>();
        for (Application a : apps) {
            if (a.getJobId() != null && "ACCEPTED".equals(a.getStatus())) {
                acceptedByJob.merge(a.getJobId(), 1L, Long::sum);
            }
        }

        int closedCount = 0;
        boolean changed = false;
        for (Job j : allJobs) {
            if (!u.getUserId().equals(j.getMoUserId())) {
                continue;
            }
            if (!"OPEN".equals(j.getStatus())) {
                continue;
            }
            int vac = j.getVacancies();
            if (vac <= 0) {
                continue;
            }
            long n = acceptedByJob.getOrDefault(j.getJobId(), 0L);
            if (n >= vac) {
                j.setStatus("CLOSED");
                closedCount++;
                changed = true;
            }
        }
        if (changed) {
            jobDAO.writeAllJobs(allJobs);
        }
        resp.sendRedirect(req.getContextPath() + "/mo/jobs.jsp?success=closedFull&n=" + closedCount);
    }
}
