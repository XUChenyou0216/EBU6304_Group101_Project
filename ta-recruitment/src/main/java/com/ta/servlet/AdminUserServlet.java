package com.ta.servlet;

import com.ta.dao.UserDAO;
import com.ta.model.User;
import com.ta.util.SessionUtil;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

/**
 * Servlet for administrators to list all users and perform account lifecycle actions.
 * <p>
 * URL mapping: {@code /admin/users} via {@link WebServlet}.
 * </p>
 *
 * @see UserDAO
 */
@WebServlet("/admin/users")
public class AdminUserServlet extends HttpServlet {

    /**
     * Loads all users and forwards to the admin user management JSP.
     *
     * @param req  the HTTP request
     * @param resp the HTTP response; forwards to {@code /admin/users.jsp}
     * @throws ServletException if the request dispatcher fails
     * @throws IOException      if forwarding fails
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String dataDir = SessionUtil.getDataDir(req);
        UserDAO dao = new UserDAO(dataDir);

        req.setAttribute("allUsers", dao.findAll());
        req.getRequestDispatcher("/admin/users.jsp").forward(req, resp);
    }

    /**
     * Processes user management actions: suspend, activate, or delete.
     *
     * @param req  the HTTP request with parameters {@code action} ({@code suspend},
     *             {@code activate}, or {@code delete}) and {@code userId}
     * @param resp the HTTP response; redirects to {@code /admin/users?success=<action>}
     * @throws ServletException if servlet processing fails
     * @throws IOException      if redirecting fails
     */
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String action = req.getParameter("action");
        String userId = req.getParameter("userId");
        String dataDir = SessionUtil.getDataDir(req);
        UserDAO dao = new UserDAO(dataDir);

        User user = dao.findById(userId);
        if (user != null) {
            switch (action) {
                case "suspend":
                    user.setStatus("SUSPENDED");
                    dao.update(user);
                    break;
                case "activate":
                    user.setStatus("ACTIVE");
                    dao.update(user);
                    break;
                case "delete":
                    dao.delete(userId);
                    break;
            }
        }

        resp.sendRedirect(req.getContextPath() + "/admin/users?success=" + action);
    }
}
