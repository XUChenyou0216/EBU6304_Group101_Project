package com.ta.servlet;

import com.ta.dao.UserDAO;
import com.ta.model.User;
import com.ta.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

/**
 * Servlet for new user self-registration with role, email, and security question fields.
 * <p>
 * URL mapping: {@code /register} via {@link WebServlet}.
 * </p>
 *
 * @see UserDAO#saveIfUsernameAvailable(User)
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    /**
     * Displays the registration form.
     *
     * @param req  the HTTP request
     * @param resp the HTTP response; forwards to {@code /register.jsp}
     * @throws ServletException if the request dispatcher fails
     * @throws IOException      if forwarding fails
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/register.jsp").forward(req, resp);
    }

    /**
     * Validates registration input, creates a new active user, and redirects to login on success.
     *
     * @param req  the HTTP request with username, password, confirmPassword, role, email,
     *             securityQuestion, and securityAnswer parameters
     * @param resp the HTTP response; forwards to register JSP on validation failure or redirects to login
     * @throws ServletException if the request dispatcher fails
     * @throws IOException      if redirecting or forwarding fails
     */
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
        User newUser = new User("", username.trim(),
            PasswordUtil.hash(password), role.toUpperCase(), email.trim(), sq, sa, "ACTIVE");
        if (!dao.saveIfUsernameAvailable(newUser)) {
            req.setAttribute("error", "Username already exists.");
            req.getRequestDispatcher("/register.jsp").forward(req, resp); return;
        }

        resp.sendRedirect(req.getContextPath() + "/login?registered=true");
    }
}
