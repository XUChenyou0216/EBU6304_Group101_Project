package com.ta.servlet;

import com.ta.dao.ApplicationDAO;
import com.ta.dao.JobDAO;
import com.ta.model.Job;
import com.ta.model.User;
import com.ta.util.JobDeadlineUtil;
import com.ta.util.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Servlet allowing a Module Organiser to re-open a CLOSED job when vacancies remain and the deadline has not passed.
 * <p>
 * URL mapping: {@code /mo/reopen-job} (declared in {@code WEB-INF/web.xml}).
 * Typical use case: after reducing accepted count manually, the job can accept new applicants again.
 * </p>
 */
public class MoReopenJobServlet extends HttpServlet {

    /**
     * Validates reopen preconditions and sets the job status to {@code OPEN}.
     *
     * @param req  the HTTP POST request with {@code jobId} and optional {@code returnTo}
     *             ({@code jobs} or applicants page)
     * @param resp the HTTP response; redirects to jobs or applicants page with success or error flags
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
        String jobId = req.getParameter("jobId");
        if (jobId == null || jobId.trim().isEmpty()) {
            sendBack(req, resp, null, "reopenMissing");
            return;
        }
        jobId = jobId.trim();
        String returnTo = req.getParameter("returnTo");

        String dataDir = SessionUtil.getDataDir(req);
        JobDAO jobDAO = new JobDAO(dataDir);
        ApplicationDAO appDAO = new ApplicationDAO(dataDir);
        Job job = jobDAO.findById(jobId);
        if (job == null || !job.getMoUserId().equals(u.getUserId())) {
            sendBack(req, resp, jobId, "reopenNotFound");
            return;
        }
        if (!"CLOSED".equals(job.getStatus())) {
            sendBack(req, resp, jobId, "reopenNotClosed");
            return;
        }
        if (JobDeadlineUtil.isPastDeadline(job.getDeadline())) {
            sendBack(req, resp, jobId, "reopenDeadline");
            return;
        }
        int vac = job.getVacancies();
        if (vac <= 0) {
            sendBack(req, resp, jobId, "reopenNoVacancy");
            return;
        }
        long accepted = appDAO.countAcceptedForJob(jobId);
        if (accepted >= vac) {
            sendBack(req, resp, jobId, "reopenStillFull");
            return;
        }

        job.setStatus("OPEN");
        jobDAO.update(job);

        String enc = URLEncoder.encode(jobId, StandardCharsets.UTF_8);
        if ("jobs".equals(returnTo)) {
            resp.sendRedirect(req.getContextPath() + "/mo/jobs.jsp?success=reopened&jobId=" + enc);
        } else {
            resp.sendRedirect(req.getContextPath() + "/mo/applicants?jobId=" + enc + "&success=reopened");
        }
    }

    private static void sendBack(HttpServletRequest req, HttpServletResponse resp, String jobId, String err)
            throws IOException {
        String enc = jobId != null && !jobId.isEmpty()
            ? URLEncoder.encode(jobId, StandardCharsets.UTF_8) : "";
        String returnTo = req.getParameter("returnTo");
        if ("jobs".equals(returnTo)) {
            resp.sendRedirect(req.getContextPath() + "/mo/jobs.jsp?error=" + err);
        } else if (enc.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/mo/applicants?error=" + err);
        } else {
            resp.sendRedirect(req.getContextPath() + "/mo/applicants?jobId=" + enc + "&error=" + err);
        }
    }
}
