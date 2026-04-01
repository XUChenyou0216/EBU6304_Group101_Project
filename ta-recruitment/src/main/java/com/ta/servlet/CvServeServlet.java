package com.ta.servlet;

import com.ta.dao.ApplicationDAO;
import com.ta.dao.JobDAO;
import com.ta.dao.TAProfileDAO;
import com.ta.model.Job;
import com.ta.model.TAProfile;
import com.ta.model.User;
import com.ta.util.CvMimeUtil;
import com.ta.util.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Serves CV files with inline (preview) or attachment (download) disposition.
 * action=own — TA views own CV; action=applicant — MO views an applicant for a job they own.
 */
@WebServlet("/cv/serve")
public class CvServeServlet extends HttpServlet {

    private static final int BUFFER = 8192;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = SessionUtil.getCurrentUser(req);
        if (user == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String action = req.getParameter("action");
        if (action == null) action = "";

        String dataDir = SessionUtil.getDataDir(req);
        TAProfileDAO profileDAO = new TAProfileDAO(dataDir);
        TAProfile profile;

        if ("own".equalsIgnoreCase(action)) {
            if (!"TA".equalsIgnoreCase(user.getRole())) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            profile = profileDAO.findByUserId(user.getUserId());
        } else if ("applicant".equalsIgnoreCase(action)) {
            if (!"MO".equalsIgnoreCase(user.getRole())) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            String jobId = req.getParameter("jobId");
            String taUserId = req.getParameter("taUserId");
            if (jobId == null || taUserId == null || jobId.isEmpty() || taUserId.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            Job job = new JobDAO(dataDir).findById(jobId);
            if (job == null || !job.getMoUserId().equals(user.getUserId())) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            ApplicationDAO appDAO = new ApplicationDAO(dataDir);
            boolean applied = appDAO.findByJob(jobId).stream()
                    .anyMatch(a -> a.getTaUserId().equals(taUserId));
            if (!applied) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            profile = profileDAO.findByUserId(taUserId);
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (profile == null || profile.getCvFilePath() == null || profile.getCvFilePath().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String rel = profile.getCvFilePath().replace('\\', '/');
        if (rel.contains("..")) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        File root = new File(req.getServletContext().getRealPath("/"));
        File uploadRoot = new File(root, "uploads").getCanonicalFile();
        File file = new File(root, rel).getCanonicalFile();
        if (!file.getPath().startsWith(uploadRoot.getPath()) || !file.isFile()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String disposition = req.getParameter("disposition");
        boolean attachment = "attachment".equalsIgnoreCase(disposition);

        String mime = CvMimeUtil.guessContentType(file.getName());
        resp.setContentType(mime);
        resp.setContentLengthLong(file.length());

        String asciiName = file.getName().replaceAll("[^a-zA-Z0-9._-]", "_");
        String encoded = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8).replace("+", "%20");
        if (attachment) {
            resp.setHeader("Content-Disposition",
                    "attachment; filename=\"" + asciiName + "\"; filename*=UTF-8''" + encoded);
        } else {
            resp.setHeader("Content-Disposition", "inline; filename=\"" + asciiName + "\"");
        }

        try (FileInputStream in = new FileInputStream(file);
             OutputStream out = resp.getOutputStream()) {
            byte[] buf = new byte[BUFFER];
            int n;
            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
        }
    }
}
