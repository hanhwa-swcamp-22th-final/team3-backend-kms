package com.ohgiraffers.team3backendkms.support;

import com.ohgiraffers.team3backendkms.jwt.EmployeeUserDetails;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

public final class SecurityTestSupport {

    private SecurityTestSupport() {
    }

    public static UsernamePasswordAuthenticationToken authToken(Long employeeId, String role) {
        EmployeeUserDetails principal = new EmployeeUserDetails(
                employeeId,
                "test-" + employeeId,
                List.of(new SimpleGrantedAuthority(role))
        );

        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    public static RequestPostProcessor authenticated(Long employeeId, String role) {
        return request -> {
            MockHttpSession session = new MockHttpSession();
            session.setAttribute("SPRING_SECURITY_CONTEXT", new SecurityContextImpl(authToken(employeeId, role)));
            request.setSession(session);
            return request;
        };
    }
}
