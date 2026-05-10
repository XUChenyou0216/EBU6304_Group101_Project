package com.ta.util;

import com.ta.model.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class SessionUtil {
    private static final String USER_KEY = "currentUser";

    public static void setCurrentUser(HttpServletRequest req, User user) {
        req.getSession().setAttribute(USER_KEY, user);
    }

    public static User getCurrentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return null;
        return (User) session.getAttribute(USER_KEY);
    }

    public static boolean isLoggedIn(HttpServletRequest req) {
        return getCurrentUser(req) != null;
    }

    public static boolean hasRole(HttpServletRequest req, String role) {
        User user = getCurrentUser(req);
        return user != null && role.equalsIgnoreCase(user.getRole());
    }

    public static void logout(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) session.invalidate();
    }

    public static String getDataDir(HttpServletRequest req) {
        return DataDirUtil.resolve(req.getServletContext());
    }
}
