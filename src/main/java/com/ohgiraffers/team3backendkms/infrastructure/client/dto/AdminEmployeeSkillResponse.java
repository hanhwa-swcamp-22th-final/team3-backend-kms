package com.ohgiraffers.team3backendkms.infrastructure.client.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminEmployeeSkillResponse {

    private Long skillId;
    private String skillName;
    private BigDecimal skillScore;
}
