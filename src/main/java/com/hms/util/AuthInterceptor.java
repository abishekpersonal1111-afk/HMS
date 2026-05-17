package com.hms.util;

import com.hms.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Session-based RBAC interceptor.
 * Applied to all /api/** routes except /api/auth/**
 */
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Unauthorized. Please log in.\"}");
            return false;
        }

        // Role checks for sensitive admin-only endpoints
        String uri = request.getRequestURI();
        String method = request.getMethod();
        User user = (User) session.getAttribute("currentUser");

        // Only ADMINs can manage users
        if (uri.contains("/api/users") && user.getRole() != User.Role.ADMIN) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Access denied. Admins only.\"}");
            return false;
        }

        // Only ADMINs can modify doctor records
        if (uri.contains("/api/doctors") && !"GET".equals(method) && user.getRole() != User.Role.ADMIN) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Access denied. Admins only.\"}");
            return false;
        }

        // Only ADMINs can access patient management APIs
        if (uri.contains("/api/patients") && user.getRole() != User.Role.ADMIN) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Access denied. Admins only.\"}");
            return false;
        }

        // Only ADMINs can modify billing records
        if (uri.contains("/api/bills") && !"GET".equals(method) && user.getRole() != User.Role.ADMIN) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Access denied. Admins only.\"}");
            return false;
        }

        // Only PATIENTs can book new appointments
        if (uri.contains("/api/appointments") && "POST".equals(method) && user.getRole() != User.Role.PATIENT) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Access denied. Patients only.\"}");
            return false;
        }

        // Patients cannot create, update, or delete prescriptions
        if (uri.contains("/api/prescriptions")
                && ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method))
                && user.getRole() == User.Role.PATIENT) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Access denied. Doctors and admins only.\"}");
            return false;
        }

        return true;
    }
}
