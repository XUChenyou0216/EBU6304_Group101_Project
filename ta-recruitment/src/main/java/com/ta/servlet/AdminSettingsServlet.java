package com.ta.servlet;

import com.ta.model.User;
import com.ta.util.ConfigDAO;
import com.ta.util.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * Servlet for administrators to view and update system configuration, especially AI integration settings.
 * <p>
 * URL mapping: {@code /admin/settings} (declared in {@code WEB-INF/web.xml}).
 * Access is restricted to users with the {@code ADMIN} role via session checks.
 * </p>
 *
 * @see ConfigDAO
 */
public class AdminSettingsServlet extends HttpServlet {

    /**
     * Displays the admin settings page with current AI provider, model, base URL, and a masked API key.
     *
     * @param req  the HTTP request; may include query parameter {@code saved=1} after a successful save
     * @param resp the HTTP response; forwards to {@code /admin/settings.jsp} on success
     * @throws ServletException if the request dispatcher fails
     * @throws IOException      if forwarding or sending an error response fails
     */

public class AdminSettingsServlet extends HttpServlet {


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User current = SessionUtil.getCurrentUser(req);
        if (current == null || !"ADMIN".equalsIgnoreCase(current.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin access required.");
            return;
        }

        String dataDir = SessionUtil.getDataDir(req);
        ConfigDAO config = new ConfigDAO(dataDir);

        String apiKey   = config.get("ai.api.key");
        String provider = config.get("ai.provider");
        String model    = config.get("ai.model");
        String baseUrl  = config.get("ai.base.url");

        req.setAttribute("config", config.getAll());
        req.setAttribute("maskedApiKey", maskKey(apiKey));
        req.setAttribute("hasApiKey", apiKey != null && !apiKey.trim().isEmpty());
        req.setAttribute("aiProvider", provider != null ? provider : "anthropic");
        req.setAttribute("aiModel", model != null ? model : "claude-haiku-4-5-20251001");
        req.setAttribute("aiBaseUrl", baseUrl != null ? baseUrl : "https://api.openai.com/v1");
        req.setAttribute("saved", "1".equals(req.getParameter("saved")));

        req.getRequestDispatcher("/admin/settings.jsp").forward(req, resp);
    }


    /**
     * Persists AI configuration submitted from the settings form.
     * The API key is only overwritten when a non-empty value without placeholder masking is supplied.
     *
     * @param req  the HTTP request with form fields {@code ai.provider}, {@code ai.api.key},
     *             {@code ai.model}, and {@code ai.base.url}
     * @param resp the HTTP response; redirects to {@code /admin/settings?saved=1} on success
     * @throws ServletException if servlet processing fails
     * @throws IOException      if redirecting or sending an error response fails
     */

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User current = SessionUtil.getCurrentUser(req);
        if (current == null || !"ADMIN".equalsIgnoreCase(current.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin access required.");
            return;
        }

        String dataDir = SessionUtil.getDataDir(req);
        ConfigDAO config = new ConfigDAO(dataDir);

        String provider = req.getParameter("ai.provider");
        String apiKey   = req.getParameter("ai.api.key");
        String model    = req.getParameter("ai.model");
        String baseUrl  = req.getParameter("ai.base.url");

        if (provider != null && !provider.trim().isEmpty())
            config.set("ai.provider", provider.trim());

        // Only overwrite key if user typed a real value (not placeholder "****")
        if (apiKey != null && !apiKey.trim().isEmpty() && !apiKey.contains("****"))
            config.set("ai.api.key", apiKey.trim());

        if (model != null && !model.trim().isEmpty())
            config.set("ai.model", model.trim());

        if (baseUrl != null && !baseUrl.trim().isEmpty())
            config.set("ai.base.url", baseUrl.trim());

        resp.sendRedirect(req.getContextPath() + "/admin/settings?saved=1");
    }

    private String maskKey(String key) {
        if (key == null || key.trim().isEmpty()) return "";
        String k = key.trim();
        if (k.length() <= 8) return "****";
        return k.substring(0, 4) + "****" + k.substring(k.length() - 4);
    }
}
