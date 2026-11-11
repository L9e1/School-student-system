package com.library.filter;

import com.library.entity.User;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 登录拦截：未登录只能访问登录、注册、静态资源和错误页。
 */
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String contextPath = req.getContextPath();
        String path = req.getRequestURI().substring(contextPath.length());

        boolean publicPath = path.equals("/login.jsp")
                || path.equals("/register.jsp")
                || path.equals("/auth")
                || path.equals("/error.jsp")
                || path.equals("/favicon.ico")
                || path.startsWith("/assets/");

        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("loginUser");

        if (user == null && !publicPath) {
            resp.sendRedirect(contextPath + "/login.jsp");
            return;
        }

        if (user != null && (path.equals("/login.jsp") || path.equals("/register.jsp"))) {
            resp.sendRedirect(contextPath + "/home");
            return;
        }

        if (user != null) {
            resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
            resp.setHeader("Pragma", "no-cache");
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
