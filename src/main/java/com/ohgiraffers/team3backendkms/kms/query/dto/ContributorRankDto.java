package com.ohgiraffers.team3backendkms.kms.query.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContributorRankDto {

    private Long employeeId;
    private String employeeName;
    private String employeeTier;
    private Long articleCount;
    private Long totalViewCount;
    private Integer rank;
}
