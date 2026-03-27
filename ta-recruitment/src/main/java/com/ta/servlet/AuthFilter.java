package com.ta.servlet;

import com.ta.model.User;
import com.ta.util.SessionUtil;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/*")
public class AuthFilter implements Filter {
    public void init(FilterConfig config) {}

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

    public void destroy() {}
}
