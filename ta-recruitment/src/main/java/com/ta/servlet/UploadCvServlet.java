package com.ta.servlet;

import com.ta.dao.TAProfileDAO;
import com.ta.model.TAProfile;
import com.ta.model.User;
import com.ta.util.SessionUtil;
import com.ta.util.Validator;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@WebServlet("/ta/upload-cv")
@MultipartConfig(maxFileSize = 10 * 1024 * 1024, maxRequestSize = 11 * 1024 * 1024)
public class UploadCvServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = SessionUtil.getCurrentUser(req);
        if (user == null || !"TA".equalsIgnoreCase(user.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        Part part = req.getPart("cvFile");
        if (part == null) {
            req.setAttribute("error", "Please select a file.");
            req.getRequestDispatcher("/ta/profile.jsp").forward(req, resp);
            return;
        }

        String submitted = "";
        String header = part.getHeader("content-disposition");
        if (header != null) {
            for (String content : header.split(";")) {
                if (content.trim().startsWith("filename")) {
                    submitted = content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
                    // 处理某些浏览器可能发送的绝对路径
                    submitted = Paths.get(submitted).getFileName().toString();
                    break;
                }
            }
        }
        // ------------------------------------------

        long size = part.getSize();
        String err = Validator.validateCvFile(submitted, size);
        if (err != null) {
            req.setAttribute("error", err);
            req.getRequestDispatcher("/ta/profile.jsp").forward(req, resp);
            return;
        }

        String ext = "";
        int dot = submitted.lastIndexOf('.');
        if (dot >= 0) ext = submitted.substring(dot).toLowerCase();

        String uploadsDir = req.getServletContext().getRealPath("/uploads");
        if (uploadsDir == null) {
            req.setAttribute("error", "Server upload path unavailable.");
            req.getRequestDispatcher("/ta/profile.jsp").forward(req, resp);
            return;
        }
        Files.createDirectories(Paths.get(uploadsDir));

        String storedName = user.getUserId() + "_" + System.currentTimeMillis() + ext;
        Path target = Paths.get(uploadsDir, storedName);

        try (InputStream in = part.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        String relative = "uploads/" + storedName;
        String dataDir = SessionUtil.getDataDir(req);
        TAProfileDAO dao = new TAProfileDAO(dataDir);
        TAProfile p = dao.findByUserId(user.getUserId());
        if (p == null) {
            p = new TAProfile(user.getUserId(), "", "", "", "", "", relative);
        } else {
            p.setCvFilePath(relative);
        }
        dao.saveOrUpdate(p);

        resp.sendRedirect(req.getContextPath() + "/ta/profile.jsp?success=true");
    }
}