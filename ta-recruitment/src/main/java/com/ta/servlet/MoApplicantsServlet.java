package com.ta.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Canonical servlet route for the MO applicants and review page.
 * <p>
 * URL mapping: {@code /mo/applicants} (declared in {@code WEB-INF/web.xml} for reliable deployment).
 * Forwards to the JSP view; application data is loaded in the JSP or related servlets.
 * </p>
 */
public class MoApplicantsServlet extends HttpServlet {

    /**
     * Forwards the request to the applicants review JSP.
     *
     * @param req  the HTTP request; optional {@code jobId} query parameter is handled by the JSP
     * @param resp the HTTP response; forwards to {@code /mo/applicants.jsp}
     * @throws ServletException if the request dispatcher fails
     * @throws IOException      if forwarding fails
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/mo/applicants.jsp").forward(req, resp);
    }
}
