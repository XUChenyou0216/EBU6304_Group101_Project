package com.ta.servlet;

import com.ta.model.User;
import com.ta.util.SessionUtil;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Authentication and role-based authorization filter applied to all application paths.
 * <p>
 * URL mapping: {@code /*} via {@link WebFilter}.
 * Unauthenticated users are redirected to the login page; authenticated users must match
 * the role required for {@code /ta/}, {@code /mo/}, or {@code /admin/} path prefixes.
 * </p>
 *
 * @see SessionUtil
 */
@WebFilter("/*")
public class AuthFilter implements Filter {

    /**
     * Initializes the filter. No configuration is required.
     *
     * @param config the filter configuration provided by the container
     */
    public void init(FilterConfig config) {}

    /**
     * Enforces login and role checks before allowing the request to proceed.
     *
     * @param req   the incoming servlet request
     * @param res   the servlet response
     * @param chain the filter chain to invoke when access is granted
     * @throws IOException      if redirecting or sending an error fails
     * @throws ServletException if filter processing fails
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String path = request.getRequestURI().substring(request.getContextPath().length());

        if (isPublic(path)) { chain.doFilter(req, res); return; }

        User user = SessionUtil.getCurrentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        if (path.startsWith("/ta/") && !"TA".equalsIgnoreCase(user.getRole())) {
            response.sendError(403, "Access Denied: TA only."); return;
        }
        if (path.startsWith("/mo/") && !"MO".equalsIgnoreCase(user.getRole())) {
            response.sendError(403, "Access Denied: MO only."); return;
        }
        if (path.startsWith("/admin/") && !"ADMIN".equalsIgnoreCase(user.getRole())) {
            response.sendError(403, "Access Denied: Admin only."); return;
        }
        chain.doFilter(req, res);
    }

    private boolean isPublic(String path) {
        return path.equals("/") || path.equals("/login") || path.equals("/login.jsp")
            || path.equals("/register") || path.equals("/register.jsp")
            || path.equals("/recover") || path.equals("/recover.jsp")
            || path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/");
    }

    /**
     * Releases filter resources. No cleanup is performed.
     */
    public void destroy() {}
}
