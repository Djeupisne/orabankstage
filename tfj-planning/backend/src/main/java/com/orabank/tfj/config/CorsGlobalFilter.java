package com.orabank.tfj.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsGlobalFilter implements Filter {

    private static final List<String> ALLOWED_ORIGINS = List.of(
        "https://tfj-planning-frontend.onrender.com",
        "http://localhost:4200",
        "http://localhost:3000"
    );

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String origin = request.getHeader("Origin");

        if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS, PATCH");
            response.setHeader("Access-Control-Allow-Headers",
                "Authorization, Content-Type, Accept, Origin, " +
                "X-Requested-With, Access-Control-Request-Method, " +
                "Access-Control-Request-Headers");
            response.setHeader("Access-Control-Expose-Headers",
                "Authorization, Content-Disposition");
            response.setHeader("Access-Control-Max-Age", "3600");
        }

        // Répondre immédiatement aux requêtes OPTIONS (preflight)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        chain.doFilter(req, res);
    }
}
