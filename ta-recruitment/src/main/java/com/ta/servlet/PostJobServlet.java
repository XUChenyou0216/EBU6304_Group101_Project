package com.ta.servlet;

import com.ta.dao.ApplicationDAO;
import com.ta.dao.JobDAO;
import com.ta.model.Application;
import com.ta.dao.NotificationDAO;
import com.ta.model.Job;
import com.ta.model.Notification;
import com.ta.model.User;
import com.ta.util.SessionUtil;
import com.ta.util.Validator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

/**
 * Servlet for Module Organisers to post new TA jobs, edit existing jobs, or close jobs.
 * <p>
 * URL mapping: {@code /mo/post-job} via {@link WebServlet}.
 * GET shows the post/edit form; POST creates, updates, or closes a job.
 * </p>
 */
@WebServlet("/mo/post-job")
public class PostJobServlet extends HttpServlet {

    /**
     * Forwards to the job posting form for authenticated MO users.
     *
     * @param request  the HTTP request
     * @param response the HTTP response; forwards to {@code /mo/post-job.jsp}
     * @throws ServletException if the request dispatcher fails
     * @throws IOException      if forwarding or sending forbidden fails
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null || !"MO".equals(currentUser.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        request.getRequestDispatcher("/mo/post-job.jsp").forward(request, response);
    }

    /**
     * Creates a new job, updates an existing job, or closes a job based on form parameters.
     *
     * @param request  the HTTP request with job fields; {@code jobId} present for edit/close,
     *                 {@code action=close} to close without deleting
     * @param response the HTTP response; redirects to {@code /mo/jobs.jsp} with success or error flags
     * @throws ServletException if servlet processing fails
     * @throws IOException      if redirecting or sending forbidden fails
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null || !"MO".equals(currentUser.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        request.setCharacterEncoding("UTF-8");

        String jobId = request.getParameter("jobId");
        String action = request.getParameter("action");
        String moduleCode = request.getParameter("moduleCode");
        String moduleName = request.getParameter("moduleName");
        String jobTitle = request.getParameter("jobTitle");
        String description = Validator.sanitizeForCsv(request.getParameter("description"));
        String vacanciesStr = request.getParameter("vacancies");
        String deadline = request.getParameter("deadline");
        String workingPeriod = Validator.sanitizeForCsv(request.getParameter("workingPeriod"));
        String keyDuties = Validator.sanitizeForCsv(request.getParameter("keyDuties"));
        String requiredSkills = Validator.sanitizeForCsv(request.getParameter("requiredSkills"));
        String eligibility = Validator.sanitizeForCsv(request.getParameter("eligibility"));

        int vacancies = 1;
        try {
            if (vacanciesStr != null && !vacanciesStr.isEmpty()) {
                vacancies = Integer.parseInt(vacanciesStr);
            }
        } catch (NumberFormatException e) {
            // default to 1
        }

        String dataDir = SessionUtil.getDataDir(request);
        JobDAO jobDAO = new JobDAO(dataDir);
        ApplicationDAO applicationDAO = new ApplicationDAO(dataDir);

        if (jobId != null && !jobId.trim().isEmpty()) {
            // Edit existing job
            Job job = jobDAO.findById(jobId);
            if (job == null || !job.getMoUserId().equals(currentUser.getUserId())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Not authorized to edit this job.");
                return;
            }

            if ("close".equalsIgnoreCase(action)) {
                if (!"OPEN".equalsIgnoreCase(job.getStatus())) {
                    response.sendRedirect(request.getContextPath() + "/mo/jobs.jsp?error=alreadyclosed");
                    return;
                }

                int acceptedCount = 0;
                for (Application application : applicationDAO.findByJob(jobId)) {
                    if ("ACCEPTED".equalsIgnoreCase(application.getStatus())) {
                        acceptedCount++;
                    }
                }

                if (acceptedCount >= Math.max(job.getVacancies(), 0)) {
                    response.sendRedirect(request.getContextPath() + "/mo/jobs.jsp?error=filled");
                    return;
                }

                job.setStatus("CLOSED");
                jobDAO.update(job);
                response.sendRedirect(request.getContextPath() + "/mo/jobs.jsp?success=closed");
                return;
            }

            // Edit existing job
            job.setModuleCode(moduleCode);
            job.setModuleName(moduleName);
            job.setJobTitle(jobTitle);
            job.setDescription(description);
            job.setRequiredSkills(requiredSkills);
            job.setVacancies(vacancies);
            job.setDeadline(deadline);
            job.setWorkingPeriod(workingPeriod);
            job.setKeyDuties(keyDuties);
            job.setEligibility(eligibility);
            // keep existing status and createdDate
            jobDAO.update(job);
            response.sendRedirect(request.getContextPath() + "/mo/jobs.jsp?success=updated");
        } else {
            // Create new job
            Job newJob = new Job(
                    jobDAO.generateNextId(),
                    currentUser.getUserId(),
                    moduleCode,
                    moduleName,
                    jobTitle,
                    description,
                    requiredSkills,
                    vacancies,
                    deadline,
                    workingPeriod,
                    keyDuties,
                    eligibility,
                    "OPEN",
                    LocalDate.now().toString()
            );
            jobDAO.save(newJob);

            // Notify the MO that the job was posted successfully
            NotificationDAO notifDao = new NotificationDAO(dataDir);
            Notification notif = new Notification(
                    notifDao.generateNextId(),
                    currentUser.getUserId(),
                    Notification.TYPE_JOB_POSTED,
                    "Job \"" + moduleName + " — " + jobTitle + "\" has been posted successfully.",
                    false,
                    LocalDate.now().toString()
            );
            notifDao.save(notif);

            response.sendRedirect(request.getContextPath() + "/mo/jobs.jsp?success=posted");
        }
    }
}
