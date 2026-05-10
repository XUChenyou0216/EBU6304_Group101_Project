package com.ta.servlet;

import com.ta.dao.UserDAO;
import com.ta.model.User;
import com.ta.util.SessionUtil;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/admin/users")
public class AdminUserServlet extends HttpServlet {

    // 加载用户列表（展示页面）
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String dataDir = SessionUtil.getDataDir(req);
        UserDAO dao = new UserDAO(dataDir);

        req.setAttribute("allUsers", dao.findAll());
        req.getRequestDispatcher("/admin/users.jsp").forward(req, resp);
    }

    // 处理用户操作（修改状态）点击禁用、激活或删除
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