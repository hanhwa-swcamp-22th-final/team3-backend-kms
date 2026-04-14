package com.ohgiraffers.team3backendkms.infrastructure.client.feign;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 *   해당 클래스의 목적:
 *   - team3-backend-kms가
 *       - team3-backend-admin
 *       - team3-backend-hr
 *         를 Feign으로 호출할 때,
 *   - 현재 로그인 사용자의 JWT를 같이 넘겨서
 *   - 대상 서비스에서도 같은 사용자 권한으로 인증/인가가 되게 만드는 역할입니다.
 */
@Configuration
public class ExternalAuthorizationForwardingConfiguration {

    @Bean
    public RequestInterceptor authorizationForwardingInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }

            HttpServletRequest request = attributes.getRequest();
            String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authorization != null && !authorization.isBlank()) {
                requestTemplate.header(HttpHeaders.AUTHORIZATION, authorization);
            }
        };
    }
}
