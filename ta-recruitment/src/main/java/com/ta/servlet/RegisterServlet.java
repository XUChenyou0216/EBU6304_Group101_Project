package com.ta.servlet;

import com.ta.dao.UserDAO;
import com.ta.model.User;
import com.ta.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/register.jsp").forward(req, resp);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String confirm  = req.getParameter("confirmPassword");
        String role     = req.getParameter("role");
        String email    = req.getParameter("email");
        String sq       = req.getParameter("securityQuestion");
        String sa       = req.getParameter("securityAnswer");

        String err = Validator.requireNonEmpty(username, "Username");
        if (err == null) err = Validator.validatePassword(password);
        if (err == null) err = Validator.validateEmail(email);
        if (err == null && !password.equals(confirm)) err = "Passwords do not match.";
        if (err == null) err = Validator.requireNonEmpty(role, "Role");
        if (err == null) err = Validator.requireNonEmpty(sq, "Security question");
        if (err == null) err = Validator.requireNonEmpty(sa, "Security answer");
        if (err != null) {
            req.setAttribute("error", err);
            req.getRequestDispatcher("/register.jsp").forward(req, resp); return;
        }

        UserDAO dao = new UserDAO(SessionUtil.getDataDir(req));
        if (dao.findByUsername(username.trim()) != null) {
            req.setAttribute("error", "Username already exists.");
            req.getRequestDispatcher("/register.jsp").forward(req, resp); return;
        }

        User newUser = new User(dao.generateNextId(), username.trim(),
            PasswordUtil.hash(password), role.toUpperCase(), email.trim(), sq, sa, "ACTIVE");
        dao.save(newUser);

        resp.sendRedirect(req.getContextPath() + "/login?registered=true");
    }
}
