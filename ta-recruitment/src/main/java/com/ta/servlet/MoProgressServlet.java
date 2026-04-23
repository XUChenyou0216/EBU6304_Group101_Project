package com.ta.servlet;

import com.ta.dao.ApplicationDAO;
import com.ta.dao.JobDAO;
import com.ta.model.Application;
import com.ta.model.Job;
import com.ta.model.JobProgressView;
import com.ta.model.User;
import com.ta.util.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Canonical route {@code /mo/progress} prepares recruitment progress metrics for the JSP view.
 */
public class MoProgressServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User currentUser = SessionUtil.getCurrentUser(req);
        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        String dataDir = SessionUtil.getDataDir(req);
        JobDAO jobDAO = new JobDAO(dataDir);
        ApplicationDAO appDAO = new ApplicationDAO(dataDir);
        List<Job> myJobs = jobDAO.findByMo(currentUser.getUserId());
        List<JobProgressView> progressRows = new ArrayList<>();

        int totalApplications = 0;
        int totalUnderReview = 0;
        int totalAccepted = 0;
        int totalVacancies = 0;

        for (Job job : myJobs) {
            List<Application> applications = appDAO.findByJob(job.getJobId());
            int applicantsCount = applications.size();
            int underReviewCount = 0;
            int acceptedCount = 0;

            for (Application application : applications) {
                String status = application.getStatus();
                if ("ACCEPTED".equalsIgnoreCase(status)) {
                    acceptedCount++;
                } else if ("UNDER_REVIEW".equalsIgnoreCase(status)) {
                    underReviewCount++;
                }
            }

            totalApplications += applicantsCount;
            totalUnderReview += underReviewCount;
            totalAccepted += acceptedCount;
            totalVacancies += Math.max(job.getVacancies(), 0);

            progressRows.add(new JobProgressView(
                    getDisplayTitle(job),
                    job.getModuleName(),
                    job.getVacancies(),
                    applicantsCount,
                    underReviewCount,
                    acceptedCount,
                    calculateFillRate(acceptedCount, job.getVacancies()),
                    "OPEN".equalsIgnoreCase(job.getStatus()) ? "active" : "closed",
                    "OPEN".equalsIgnoreCase(job.getStatus()) ? "Active" : "Closed"
            ));
        }

        req.setAttribute("progressRows", progressRows);
        req.setAttribute("totalApplications", totalApplications);
        req.setAttribute("totalUnderReview", totalUnderReview);
        req.setAttribute("totalAccepted", totalAccepted);
        req.setAttribute("overallFillRate", calculateFillRate(totalAccepted, totalVacancies));
        req.getRequestDispatcher("/mo/progress.jsp").forward(req, resp);
    }

    private String getDisplayTitle(Job job) {
        String jobTitle = job.getJobTitle();
        if (jobTitle != null && !jobTitle.trim().isEmpty()) {
            return jobTitle;
        }
        return job.getModuleName();
    }

    private int calculateFillRate(int acceptedCount, int vacancies) {
        if (vacancies <= 0) {
            return 0;
        }

        int fillRate = (int) Math.round(acceptedCount * 100.0 / vacancies);
        return Math.min(fillRate, 100);
    }
}
