/*
 * AI-assisted: Claude (Anthropic), 2026-11-09.
 */
package com.bbr.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Runs before every /api request and checks the JWT cookie. */
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwt;

    public JwtFilter(JwtService jwt) {
        this.jwt = jwt;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        String path = req.getRequestURI();
        return !path.startsWith("/api/")
                || path.startsWith("/api/auth/")        // register, login, OTP are public
                || path.equals("/api/levels")
                || path.equals("/api/leaderboard");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String token = null;
        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) {
                if ("token".equals(c.getName())) token = c.getValue();
            }
        }
        Long userId;
        try {
            userId = jwt.userId(token);
        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json");
            res.getWriter().write("{\"message\":\"Please log in\"}");
            return;
        }
        req.setAttribute("userId", userId);   // controllers read it with @RequestAttribute
        chain.doFilter(req, res);
    }
}
