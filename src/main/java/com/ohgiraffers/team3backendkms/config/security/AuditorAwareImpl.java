package com.ohgiraffers.team3backendkms.config.security;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("auditorProvider")
public class AuditorAwareImpl implements AuditorAware<Long> {


    // JPA save() 실행 시 호출 — SecurityContext에서 현재 사용자의 employeeId를 반환, 인증 없으면 null 저장
    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 인증 정보 없음 (익명 요청, 시스템 작업)
        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = auth.getPrincipal();

        // 정상 인증 요청 — CustomUserDetails에서 employeeId 추출
        if (principal instanceof CustomUserDetails customUserDetails) {
            return Optional.of(customUserDetails.getEmployeeId());
        }

        // 그 외 (anonymousUser 등)
        return Optional.empty();
    }
}
