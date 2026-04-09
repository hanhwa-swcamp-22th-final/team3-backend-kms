package com.ohgiraffers.team3backendkms.kms.query.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApprovalStatsDto {

    private Long pendingCount;
    private Long approvedThisMonth;
    private Double rejectionRate;
}
