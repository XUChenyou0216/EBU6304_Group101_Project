package com.ta.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Canonical route {@code /mo/applicants} forwards to the JSP view.
 * Registered in {@code WEB-INF/web.xml} for reliable deployment.
 */
public class MoApplicantsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/mo/applicants.jsp").forward(req, resp);
    }
}
