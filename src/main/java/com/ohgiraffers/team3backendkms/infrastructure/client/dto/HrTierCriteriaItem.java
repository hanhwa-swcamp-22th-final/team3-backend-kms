package com.ohgiraffers.team3backendkms.infrastructure.client.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HrTierCriteriaItem {

    private Long tierConfigId;
    private String tier;
    private Integer tierConfigPromotionPoint;
    private Double equipmentResponseTargetScore;
    private Double technicalTransferTargetScore;
    private Double innovationProposalTargetScore;
    private Double safetyComplianceTargetScore;
    private Double qualityManagementTargetScore;
    private Double productivityTargetScore;
}
