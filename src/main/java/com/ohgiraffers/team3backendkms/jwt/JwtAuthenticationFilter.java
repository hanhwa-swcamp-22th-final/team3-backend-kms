package com.ohgiraffers.team3backendkms.jwt;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ohgiraffers.team3backendkms.common.dto.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String SESSION_SUPERSEDED_CODE = "AUTH_005";
    private static final String SESSION_SUPERSEDED_MESSAGE = "다른 기기에서 새로운 로그인이 감지되었습니다. 다시 로그인해 주세요.";

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthRepository authRepository;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = getJwtFromRequest(request);

        if (StringUtils.hasText(token)) {
            try {
                if (jwtTokenProvider.validateToken(token)) {
                    Long employeeId = jwtTokenProvider.getEmployeeIdFromJWT(token);
                    String employeeCode = jwtTokenProvider.getEmployeeCodeFromJWT(token);
                    String role = jwtTokenProvider.getRoleFromJWT(token);
                    String tokenSessionId = jwtTokenProvider.getLoginSessionIdFromJWT(token);

                    if (StringUtils.hasText(tokenSessionId)) {
                        Optional<RefreshToken> storedTokenOpt = authRepository.findById(employeeCode);
                        if (storedTokenOpt.isEmpty()
                                || isSupersededSession(tokenSessionId, storedTokenOpt.get().getLoginSessionId())) {
                            log.warn("다른 기기에서 로그인하여 세션이 만료됨: {}", employeeCode);
                            writeSessionSupersededResponse(response);
                            return;
                        }
                    }

                    EmployeeUserDetails userDetails = new EmployeeUserDetails(
                            employeeId,
                            employeeCode,
                            Collections.singleton(new SimpleGrantedAuthority(role))
                    );

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities()
                            );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                log.warn("JWT 인증 실패: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isSupersededSession(String tokenSessionId, String dbSessionId) {
        return dbSessionId != null && !dbSessionId.equals(tokenSessionId);
    }

    private void writeSessionSupersededResponse(HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(objectMapper.writeValueAsString(
                ApiResponse.failure(SESSION_SUPERSEDED_CODE, SESSION_SUPERSEDED_MESSAGE)
        ));
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
