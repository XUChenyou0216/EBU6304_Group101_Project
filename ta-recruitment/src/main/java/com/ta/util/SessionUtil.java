package com.ta.util;

import com.ta.model.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Helper methods for managing authenticated user state in HTTP sessions.
 * <p>
 * The currently logged-in {@link User} is stored under the session attribute key
 * {@code "currentUser"}. All methods operate on the session associated with the
 * given {@link HttpServletRequest}.
 * </p>
 */
public class SessionUtil {
    private static final String USER_KEY = "currentUser";

    /**
     * Stores the authenticated user in the HTTP session, creating a session if necessary.
     *
     * @param req  the incoming servlet request
     * @param user the authenticated user to associate with the session
     */
    public static void setCurrentUser(HttpServletRequest req, User user) {
        req.getSession().setAttribute(USER_KEY, user);
    }

    /**
     * Retrieves the currently authenticated user from the session.
     *
     * @param req the incoming servlet request
     * @return the logged-in {@link User}, or {@code null} if no session exists or the user
     *         is not logged in
     */
    public static User getCurrentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return null;
        return (User) session.getAttribute(USER_KEY);
    }

    /**
     * Determines whether a user is currently authenticated.
     *
     * @param req the incoming servlet request
     * @return {@code true} if a current user is present in the session
     */
    public static boolean isLoggedIn(HttpServletRequest req) {
        return getCurrentUser(req) != null;
    }

    /**
     * Checks whether the current user has the specified role.
     * <p>
     * Role comparison is case-insensitive.
     * </p>
     *
     * @param req  the incoming servlet request
     * @param role the role name to check (e.g. {@code "STUDENT"}, {@code "MO"})
     * @return {@code true} if a user is logged in and their role matches {@code role}
     */
    public static boolean hasRole(HttpServletRequest req, String role) {
        User user = getCurrentUser(req);
        return user != null && role.equalsIgnoreCase(user.getRole());
    }

    /**
     * Invalidates the current HTTP session, effectively logging the user out.
     * <p>
     * If no session exists, this method has no effect.
     * </p>
     *
     * @param req the incoming servlet request
     */
    public static void logout(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) session.invalidate();
    }

    /**
     * Resolves the absolute path to the CSV data directory for the current web application.
     *
     * @param req the incoming servlet request (used to obtain the {@link javax.servlet.ServletContext})
     * @return the absolute filesystem path to the data directory
     * @see DataDirUtil#resolve(javax.servlet.ServletContext)
     */
    public static String getDataDir(HttpServletRequest req) {
        return DataDirUtil.resolve(req.getServletContext());
    }
}
