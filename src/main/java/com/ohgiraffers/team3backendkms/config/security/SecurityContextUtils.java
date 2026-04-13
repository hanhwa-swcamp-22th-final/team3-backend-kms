package com.ohgiraffers.team3backendkms.config.security;

import com.ohgiraffers.team3backendkms.jwt.EmployeeUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class SecurityContextUtils {

    private static final String SECURITY_CONTEXT_SESSION_KEY = "SPRING_SECURITY_CONTEXT";

    private SecurityContextUtils() {
    }

    public static EmployeeUserDetails currentUser(Authentication authentication) {
        Authentication resolved = authentication;

        if (resolved == null) {
            resolved = SecurityContextHolder.getContext().getAuthentication();
        }

        if (resolved == null) {
            resolved = sessionAuthentication();
        }

        if (resolved == null || !(resolved.getPrincipal() instanceof EmployeeUserDetails userDetails)) {
            return null;
        }

        return userDetails;
    }

    private static Authentication sessionAuthentication() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            return null;
        }

        HttpServletRequest request = servletRequestAttributes.getRequest();
        if (request == null || request.getSession(false) == null) {
            return null;
        }

        Object context = request.getSession(false).getAttribute(SECURITY_CONTEXT_SESSION_KEY);
        if (context instanceof SecurityContext securityContext) {
            return securityContext.getAuthentication();
        }

        return null;
    }
}
