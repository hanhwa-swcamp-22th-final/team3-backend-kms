package com.ohgiraffers.team3backendkms.kms.query.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KnowledgeHubStatsDto {

    private Long totalArticles;
    private Long newThisMonth;
    private Double averageViewCount;
    private Long newThisMonthChange;
    private Double averageViewCountChange;
}
