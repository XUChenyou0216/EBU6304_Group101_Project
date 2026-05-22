package com.ta.servlet;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

/**
 * Servlet filter that sets UTF-8 character encoding on every request and response.
 * <p>
 * URL mapping: {@code /*} via {@link WebFilter}.
 * Ensures form data and rendered pages use consistent Unicode encoding.
 * </p>
 */
@WebFilter("/*")
public class EncodingFilter implements Filter {

    /**
     * Initializes the filter. No configuration is required.
     *
     * @param config the filter configuration provided by the container
     */
    public void init(FilterConfig config) {}

    /**
     * Sets {@code UTF-8} as the character encoding for the request and response, then continues the chain.
     *
     * @param req   the incoming servlet request
     * @param res   the servlet response
     * @param chain the filter chain to invoke after encoding is applied
     * @throws IOException      if filter processing fails
     * @throws ServletException if filter processing fails
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        req.setCharacterEncoding("UTF-8");
        res.setCharacterEncoding("UTF-8");
        chain.doFilter(req, res);
    }

    /**
     * Releases filter resources. No cleanup is performed.
     */
    public void destroy() {}
}
