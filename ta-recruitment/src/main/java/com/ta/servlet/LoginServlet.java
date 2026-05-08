package com.ta.servlet;

import com.ta.dao.UserDAO;
import com.ta.model.User;
import com.ta.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet(urlPatterns = {"/login", "/logout"})
public class LoginServlet extends HttpServlet {

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if ("/logout".equals(req.getServletPath())) {
            SessionUtil.logout(req);
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }
        if (SessionUtil.isLoggedIn(req)) { redirectToDashboard(req, resp); return; }
        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        long currentTime = System.currentTimeMillis();
        Long lockUntil = (Long) session.getAttribute("lock_until");
        if (lockUntil != null && currentTime < lockUntil) {
            long remainingMins = (lockUntil - currentTime) / (60 * 1000) + 1;
            req.setAttribute("error", "Too many failed attempts. Try again in " + remainingMins + " minutes.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        String err = Validator.requireNonEmpty(username, "Username");
        if (err == null) err = Validator.requireNonEmpty(password, "Password");
        if (err != null) {
            req.setAttribute("error", err);
            req.getRequestDispatcher("/login.jsp").forward(req, resp); return;
        }

        UserDAO dao = new UserDAO(SessionUtil.getDataDir(req));
        User user = dao.findByUsername(username.trim());

        if (user == null || !PasswordUtil.verify(password, user.getPasswordHash())) {
            // 登录失败处理
            Integer attempts = (Integer) session.getAttribute("login_attempts");
            if (attempts == null) attempts = 0;
            attempts++;

            if (attempts >= 5) {
                session.setAttribute("lock_until", currentTime + (5 * 60 * 1000));
                session.removeAttribute("login_attempts"); // 锁定后重置计数
                req.setAttribute("error", "Too many failed attempts. Account locked for 5 minutes.");
            } else {
                session.setAttribute("login_attempts", attempts);
                req.setAttribute("error", "Invalid username or password.");
            }

            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }
        if ("SUSPENDED".equals(user.getStatus())) {
            req.setAttribute("error", "Account suspended. Contact admin.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp); return;
        }

        session.removeAttribute("login_attempts");
        session.removeAttribute("lock_until");

        SessionUtil.setCurrentUser(req, user);
        redirectToDashboard(req, resp);
    }

    private void redirectToDashboard(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = SessionUtil.getCurrentUser(req);
        String ctx = req.getContextPath();
        if (user == null || user.getRole() == null) {
            resp.sendRedirect(ctx + "/login.jsp");
            return;
        }

        switch (user.getRole().toUpperCase()) {
            case "TA":    resp.sendRedirect(ctx + "/ta/dashboard.jsp"); break;
            case "MO":    resp.sendRedirect(ctx + "/mo/dashboard.jsp"); break;
            case "ADMIN": resp.sendRedirect(ctx + "/admin/dashboard.jsp"); break;
            default:      resp.sendRedirect(ctx + "/login.jsp");
        }
    }
}
