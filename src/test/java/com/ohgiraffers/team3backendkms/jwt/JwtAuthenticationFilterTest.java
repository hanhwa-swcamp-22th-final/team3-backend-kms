package com.ohgiraffers.team3backendkms.jwt;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String TOKEN = "access-token";
    private static final String EMPLOYEE_CODE = "EMP-0001";

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthRepository authRepository;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        filter = new JwtAuthenticationFilter(jwtTokenProvider, authRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("토큰과 DB의 loginSessionId가 일치하면 인증을 설정한다")
    void matchingLoginSessionIdSetsAuthentication() throws Exception {
        givenValidToken("session-id");
        given(authRepository.findById(EMPLOYEE_CODE)).willReturn(Optional.of(refreshToken("session-id")));

        filter.doFilter(requestWithBearerToken(), new MockHttpServletResponse(), new MockFilterChain());

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(EMPLOYEE_CODE, SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    @DisplayName("토큰과 DB의 loginSessionId가 다르면 AUTH_005를 반환한다")
    void mismatchedLoginSessionIdReturnsSessionSuperseded() throws Exception {
        givenValidToken("old-session-id");
        given(authRepository.findById(EMPLOYEE_CODE)).willReturn(Optional.of(refreshToken("current-session-id")));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(requestWithBearerToken(), response, new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("AUTH_005"));
    }

    private void givenValidToken(String loginSessionId) {
        given(jwtTokenProvider.validateToken(TOKEN)).willReturn(true);
        given(jwtTokenProvider.getEmployeeIdFromJWT(TOKEN)).willReturn(1L);
        given(jwtTokenProvider.getEmployeeCodeFromJWT(TOKEN)).willReturn(EMPLOYEE_CODE);
        given(jwtTokenProvider.getRoleFromJWT(TOKEN)).willReturn("ADMIN");
        given(jwtTokenProvider.getLoginSessionIdFromJWT(TOKEN)).willReturn(loginSessionId);
    }

    private MockHttpServletRequest requestWithBearerToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + TOKEN);
        return request;
    }

    private RefreshToken refreshToken(String loginSessionId) {
        return RefreshToken.builder()
                .employeeCode(EMPLOYEE_CODE)
                .token("refresh-token")
                .loginSessionId(loginSessionId)
                .expiryDate(new Date(System.currentTimeMillis() + 604800000L))
                .build();
    }
}
