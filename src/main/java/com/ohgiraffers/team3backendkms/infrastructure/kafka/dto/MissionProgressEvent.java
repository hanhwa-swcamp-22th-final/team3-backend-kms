package com.ohgiraffers.team3backendkms.infrastructure.kafka.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionProgressEvent {

    private Long employeeId;
    private String missionType;
    private BigDecimal progressValue;
    private boolean absolute;
}
