package com.ohgiraffers.team3backendkms.infrastructure.client;

import com.ohgiraffers.team3backendkms.infrastructure.client.dto.AdminEmployeeProfileResponse;
import com.ohgiraffers.team3backendkms.infrastructure.client.dto.AdminEmployeeSkillResponse;
import com.ohgiraffers.team3backendkms.infrastructure.client.dto.ExternalApiResponse;
import com.ohgiraffers.team3backendkms.infrastructure.client.feign.AdminFeignApi;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "feign.admin", name = "url")
public class AdminFeignClient implements AdminClient {

    private final AdminFeignApi adminFeignApi;

    @Override
    public AdminEmployeeProfileResponse getEmployeeProfile(Long employeeId) {
        ExternalApiResponse<AdminEmployeeProfileResponse> response = adminFeignApi.getEmployeeProfile(employeeId);
        return response != null ? response.getData() : null;
    }

    @Override
    public List<AdminEmployeeSkillResponse> getEmployeeSkills(Long employeeId) {
        ExternalApiResponse<List<AdminEmployeeSkillResponse>> response = adminFeignApi.getEmployeeSkills(employeeId);
        return response != null && response.getData() != null ? response.getData() : List.of();
    }
}
