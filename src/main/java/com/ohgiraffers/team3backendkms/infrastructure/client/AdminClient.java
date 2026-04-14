package com.ohgiraffers.team3backendkms.infrastructure.client;

import com.ohgiraffers.team3backendkms.infrastructure.client.dto.AdminEmployeeProfileResponse;
import com.ohgiraffers.team3backendkms.infrastructure.client.dto.AdminEmployeeSkillResponse;
import java.util.List;

public interface AdminClient {

    AdminEmployeeProfileResponse getEmployeeProfile(Long employeeId);

    List<AdminEmployeeSkillResponse> getEmployeeSkills(Long employeeId);
}
