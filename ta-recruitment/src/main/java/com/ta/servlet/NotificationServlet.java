package com.ta.servlet;

import com.ta.dao.NotificationDAO;
import com.ta.model.Notification;
import com.ta.model.User;
import com.ta.util.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * GET  /notifications        — returns current user's notifications as JSON
 * POST /notifications        — marks notification(s) as read
 *   params: action=markRead&id=NTF001  OR  action=markAllRead
 */
@WebServlet("/notifications")
public class NotificationServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = SessionUtil.getCurrentUser(req);
        if (user == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        String dataDir = SessionUtil.getDataDir(req);
        List<Notification> list = new NotificationDAO(dataDir).findByUser(user.getUserId());

        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.print("[");
        for (int i = 0; i < list.size(); i++) {
            Notification n = list.get(list.size() - 1 - i); // newest first
            out.print("{");
            out.print("\"id\":\"" + esc(n.getNotificationId()) + "\",");
            out.print("\"type\":\"" + esc(n.getType()) + "\",");
            out.print("\"message\":\"" + esc(n.getMessage()) + "\",");
            out.print("\"isRead\":" + n.isRead() + ",");
            out.print("\"createdDate\":\"" + esc(n.getCreatedDate()) + "\"");
            out.print("}");
            if (i < list.size() - 1) out.print(",");
        }
        out.print("]");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = SessionUtil.getCurrentUser(req);
        if (user == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        String dataDir = SessionUtil.getDataDir(req);
        NotificationDAO dao = new NotificationDAO(dataDir);

        String action = req.getParameter("action");
        if ("markAllRead".equals(action)) {
            dao.markAllRead(user.getUserId());
        } else if ("markRead".equals(action)) {
            String id = req.getParameter("id");
            if (id != null && !id.isEmpty()) {
                dao.markRead(id);
            }
        }

        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().print("{\"ok\":true}");
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}