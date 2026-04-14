package com.ohgiraffers.team3backendkms.infrastructure.client.feign;

import com.ohgiraffers.team3backendkms.infrastructure.client.dto.AdminEmployeeProfileResponse;
import com.ohgiraffers.team3backendkms.infrastructure.client.dto.AdminEmployeeSkillResponse;
import com.ohgiraffers.team3backendkms.infrastructure.client.dto.ExternalApiResponse;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "kmsAdminFeignApi",
        url = "${feign.admin.url}",
        configuration = ExternalAuthorizationForwardingConfiguration.class
)
public interface AdminFeignApi {

    @GetMapping("/api/v1/admin/employees/{employeeId}/profile")
    ExternalApiResponse<AdminEmployeeProfileResponse> getEmployeeProfile(@PathVariable Long employeeId);

    @GetMapping("/api/v1/admin/employees/{employeeId}/skills")
    ExternalApiResponse<List<AdminEmployeeSkillResponse>> getEmployeeSkills(@PathVariable Long employeeId);
}
