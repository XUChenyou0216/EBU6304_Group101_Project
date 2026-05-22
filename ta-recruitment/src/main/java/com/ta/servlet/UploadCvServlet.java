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

/**
 * Servlet for Teaching Assistants to upload a CV file (multipart form).
 * <p>
 * URL mapping: {@code /ta/upload-cv} via {@link WebServlet}.
 * Maximum upload size is 11 MB. Accepted formats are validated by {@link Validator#validateCvFile}.
 * </p>
 */
@WebServlet("/ta/upload-cv")
@MultipartConfig(maxFileSize = 11 * 1024 * 1024)
public class UploadCvServlet extends HttpServlet {

    /**
     * Receives a CV file upload, stores it under {@code uploads/}, and updates the TA profile path.
     *
     * @param req  the HTTP multipart request with part name {@code cvFile}
     * @param resp the HTTP response; redirects to profile JSP with success or error query parameters
     * @throws ServletException if multipart parsing fails
     * @throws IOException      if file I/O or redirect fails
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = SessionUtil.getCurrentUser(req);
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        Part part = req.getPart("cvFile");
        if (part == null || part.getSize() <= 0) {
            resp.sendRedirect(req.getContextPath() + "/ta/profile.jsp");
            return;
        }

        // 1. 解析原始文件名
        String submittedName = "";
        String contentDisp = part.getHeader("content-disposition");
        for (String token : contentDisp.split(";")) {
            if (token.trim().startsWith("filename")) {
                submittedName = token.substring(token.indexOf('=') + 1).trim().replace("\"", "");
                submittedName = Paths.get(submittedName).getFileName().toString();
                break;
            }
        }

        // 2. 统一通过 Validator 校验格式与大小（与后端规范保持一致）
        String validationError = Validator.validateCvFile(submittedName, part.getSize());
        if (validationError != null) {
            // 区分错误类型以便前端显示对应消息
            if (validationError.contains("10MB") || validationError.contains("limit")) {
                resp.sendRedirect(req.getContextPath() + "/ta/profile.jsp?error=too_large");
            } else {
                resp.sendRedirect(req.getContextPath() + "/ta/profile.jsp?error=invalid_format");
            }
            return;
        }

        // 3. 提取已验证的后缀名
        String ext = submittedName.substring(submittedName.lastIndexOf(".")).toLowerCase();

        // 4. 准备存储路径
        String uploadsPath = req.getServletContext().getRealPath("/uploads");
        java.io.File uploadDir = new java.io.File(uploadsPath);
        if (!uploadDir.exists()) uploadDir.mkdirs();

        // 5. 生成唯一文件名并保存
        String fileName = user.getUserId() + "_" + System.currentTimeMillis() + ext;
        Path target = Paths.get(uploadsPath, fileName);

        try (InputStream in = part.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        // 6. 更新数据库/CSV 记录
        String dataDir = SessionUtil.getDataDir(req);
        TAProfileDAO dao = new TAProfileDAO(dataDir);
        TAProfile p = dao.findByUserId(user.getUserId());

        if (p == null) {
            p = new TAProfile();
            p.setUserId(user.getUserId());
        }
        p.setCvFilePath("uploads/" + fileName);
        dao.saveOrUpdate(p);

        // 7. 成功跳转，注意参数名要与 profile.jsp 对应
        resp.sendRedirect(req.getContextPath() + "/ta/profile.jsp?uploadStatus=success");
    }
}
