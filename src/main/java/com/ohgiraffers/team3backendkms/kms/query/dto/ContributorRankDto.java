package com.ohgiraffers.team3backendkms.kms.query.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContributorRankDto {

    private Long employeeId;
    private String employeeName;
    private Long articleCount;
    private Integer rank;
}
