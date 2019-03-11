package com.ywr.logging.mvc.filter;

import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by zhanglin on 2019-03-08
 */
public class URILogFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        StringBuilder msg = new StringBuilder();
        String method = request.getMethod();
        if (method != null) {
            msg.append("method=").append(method + "; ");
        }
        String contentType = request.getContentType();
        if (contentType != null) {
            msg.append("contentType=").append(contentType + "; ");
        }
        msg.append("uri=").append(request.getRequestURI() + ";");
        String queryString = request.getQueryString();
        if (queryString != null) {
            msg.append('?').append(queryString + ";");
        }
        logger.info("[REQUEST URI]:" + msg.toString());
        filterChain.doFilter(request, response);
    }
}
