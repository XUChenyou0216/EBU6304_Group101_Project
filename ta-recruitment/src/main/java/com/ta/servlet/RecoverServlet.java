package com.ta.servlet;

import com.ta.dao.UserDAO;
import com.ta.model.User;
import com.ta.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/recover")
public class RecoverServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // 跳转到现有的 recover.jsp 页面
        req.getRequestDispatcher("/recover.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = req.getParameter("username");
        String securityAnswer = req.getParameter("securityAnswer");
        String newPassword = req.getParameter("newPassword");

        // 1. 验证输入项是否为空或符合格式 [cite: 32-34]
        String err = Validator.requireNonEmpty(username, "Username");
        if (err == null) err = Validator.requireNonEmpty(securityAnswer, "Security answer");
        if (err == null) err = Validator.validatePassword(newPassword);

        if (err != null) {
            req.setAttribute("error", err);
            req.getRequestDispatcher("/recover.jsp").forward(req, resp);
            return;
        }

        // 2. 查找用户 [cite: 32-33]
        UserDAO dao = new UserDAO(SessionUtil.getDataDir(req));
        User user = dao.findByUsername(username.trim());

        // 3. 校验：用户是否存在 以及 安全问题答案是否正确（忽略大小写并去空格）
        if (user == null || user.getSecurityAnswer() == null ||
                !user.getSecurityAnswer().trim().equalsIgnoreCase(securityAnswer.trim())) {
            req.setAttribute("error", "Invalid username or security answer.");
            req.getRequestDispatcher("/recover.jsp").forward(req, resp);
            return;
        }

        // 4. 更新密码哈希值并同步到数据源 [cite: 34-36]
        user.setPasswordHash(PasswordUtil.hash(newPassword));
        dao.update(user); // 确保 DAO 中实现了 update 方法以写回 CSV

        req.setAttribute("success", "Password reset successful! Please log in.");
        req.getRequestDispatcher("/recover.jsp").forward(req, resp);
    }
}