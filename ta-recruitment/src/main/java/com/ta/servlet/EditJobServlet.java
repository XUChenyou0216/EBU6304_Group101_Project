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

@WebServlet("/mo/edit-job")
public class EditJobServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null || !"MO".equals(currentUser.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String jobId = request.getParameter("jobId");
        if (jobId == null || jobId.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/mo/jobs.jsp");
            return;
        }

        String dataDir = SessionUtil.getDataDir(request);
        JobDAO jobDAO = new JobDAO(dataDir);
        Job job = jobDAO.findById(jobId);

        if (job == null) {
            response.sendRedirect(request.getContextPath() + "/mo/jobs.jsp");
            return;
        }

        if (!job.getMoUserId().equals(currentUser.getUserId())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Not authorized to edit this job.");
            return;
        }

        request.setAttribute("job", job);
        request.getRequestDispatcher("/mo/edit-job.jsp").forward(request, response);
    }
}
