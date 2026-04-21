package com.ta.servlet;

import com.ta.dao.JobDAO;
import com.ta.model.Job;
import com.ta.model.User;
import com.ta.util.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

@WebServlet("/mo/post-job")
public class PostJobServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null || !"MO".equals(currentUser.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        request.getRequestDispatcher("/mo/post-job.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null || !"MO".equals(currentUser.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        request.setCharacterEncoding("UTF-8");

        String jobId = request.getParameter("jobId");
        String moduleCode = request.getParameter("moduleCode");
        String moduleName = request.getParameter("moduleName");
        String jobTitle = request.getParameter("jobTitle");
        String description = request.getParameter("description");
        String vacanciesStr = request.getParameter("vacancies");
        String deadline = request.getParameter("deadline");
        String workingPeriod = request.getParameter("workingPeriod");
        String keyDuties = request.getParameter("keyDuties");
        String requiredSkills = request.getParameter("requiredSkills");
        String eligibility = request.getParameter("eligibility");

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

        if (jobId != null && !jobId.trim().isEmpty()) {
            // Edit existing job
            Job job = jobDAO.findById(jobId);
            if (job == null || !job.getMoUserId().equals(currentUser.getUserId())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Not authorized to edit this job.");
                return;
            }
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
            response.sendRedirect(request.getContextPath() + "/mo/jobs.jsp?success=posted");
        }
    }
}
