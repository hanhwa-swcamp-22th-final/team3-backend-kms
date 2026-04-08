package com.ohgiraffers.team3backendkms.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("auditorProvider")
public class AuditorAwareImpl implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        // 현재 프로젝트는 인증/인가를 적용하지 않으므로 감사자 정보도 비워 둔다.
        return Optional.empty();
    }
}
