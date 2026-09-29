package com.opentext.guesstheword.config;

import com.opentext.guesstheword.model.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * Guards a route: pass {@code null} for requiredRole to only require "logged in",
 * or a specific {@link Role} to also require that role. API paths (starting with
 * "/api/") get a JSON 401/403; page paths get redirected to /login or /.
 */
public class AuthInterceptor implements HandlerInterceptor {

    private final Role requiredRole;

    public AuthInterceptor(Role requiredRole) {
        this.requiredRole = requiredRole;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        boolean isApi = request.getRequestURI().startsWith("/api/");
        HttpSession session = request.getSession(false);
        Object userId = session == null ? null : session.getAttribute("userId");

        if (userId == null) {
            if (isApi) {
                writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Please log in.");
            } else {
                response.sendRedirect(request.getContextPath() + "/login");
            }
            return false;
        }
        if (requiredRole != null) {
            Object role = session.getAttribute("role");
            if (!requiredRole.label().equals(role)) {
                if (isApi) {
                    writeJsonError(response, HttpServletResponse.SC_FORBIDDEN, "Not allowed.");
                } else {
                    response.sendRedirect(request.getContextPath() + "/");
                }
                return false;
            }
        }
        return true;
    }

    private void writeJsonError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"error\":\"" + message.replace("\"", "'") + "\"}");
    }
}
