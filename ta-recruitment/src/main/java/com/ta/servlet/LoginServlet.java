package com.ta.servlet;

import com.ta.dao.UserDAO;
import com.ta.model.User;
import com.ta.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

/**
 * Servlet for user authentication: login form display, credential verification, session creation,
 * and logout.
 * <p>
 * URL mappings: {@code /login} and {@code /logout} via {@link WebServlet}.
 * Implements per-username lockout after repeated failed attempts within the same HTTP session.
 * </p>
 *
 * @see SessionUtil
 */
@WebServlet(urlPatterns = {"/login", "/logout"})
public class LoginServlet extends HttpServlet {

    /**
     * Handles logout, redirects already-authenticated users to their dashboard, or shows the login page.
     *
     * @param req  the HTTP request; servlet path {@code /logout} clears the session
     * @param resp the HTTP response; redirects or forwards to {@code /login.jsp}
     * @throws ServletException if the request dispatcher fails
     * @throws IOException      if redirecting or forwarding fails
     */
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

    /**
     * Validates credentials, enforces account lockout and suspension checks, and establishes the session.
     *
     * @param req  the HTTP request with {@code username} and {@code password} form fields
     * @param resp the HTTP response; forwards to login JSP on failure or redirects to role dashboard on success
     * @throws ServletException if the request dispatcher fails
     * @throws IOException      if redirecting or forwarding fails
     */
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        long currentTime = System.currentTimeMillis();

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        // 1. 基础非空校验
        String err = Validator.requireNonEmpty(username, "Username");
        if (err == null) err = Validator.requireNonEmpty(password, "Password");
        if (err != null) {
            req.setAttribute("error", err);
            req.getRequestDispatcher("/login.jsp").forward(req, resp); return;
        }

        String cleanUser = username.trim();
        String lockKey = "lock_until_" + cleanUser;
        String attemptKey = "login_attempts_" + cleanUser;

        // 2. 核心拦截逻辑：针对当前输入的 username 检查锁定
        Long lockUntil = (Long) session.getAttribute(lockKey);
        if (lockUntil != null) {
            if (currentTime < lockUntil) {
                long remainingMins = (lockUntil - currentTime) / (60 * 1000) + 1;
                req.setAttribute("error", "Account " + cleanUser + " is locked. Try again in " + remainingMins + " minutes.");
                req.getRequestDispatcher("/login.jsp").forward(req, resp);
                return;
            } else {
                // 时间已过，主动清理过期锁
                session.removeAttribute(lockKey);
            }
        }

        UserDAO dao = new UserDAO(SessionUtil.getDataDir(req));
        User user = dao.findByUsername(cleanUser);

        // 3. 验证逻辑
        if (user == null || !PasswordUtil.verify(password, user.getPasswordHash())) {
            Integer attempts = (Integer) session.getAttribute(attemptKey);
            if (attempts == null) attempts = 0;
            attempts++;

            if (attempts >= 5) {
                session.setAttribute(lockKey, currentTime + (5 * 60 * 1000));
                session.removeAttribute(attemptKey);
                req.setAttribute("error", "Too many failed attempts. " + cleanUser + " is locked.");
            } else {
                session.setAttribute(attemptKey, attempts);
                req.setAttribute("error", "Invalid username or password.");
            }
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        // 4. 账户状态检查
        if ("SUSPENDED".equals(user.getStatus())) {
            req.setAttribute("error", "Account suspended.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp); return;
        }

        session.removeAttribute(attemptKey);
        session.removeAttribute(lockKey);

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
