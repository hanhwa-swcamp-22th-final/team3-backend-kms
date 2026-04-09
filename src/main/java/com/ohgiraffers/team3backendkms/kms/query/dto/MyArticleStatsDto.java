package com.ohgiraffers.team3backendkms.kms.query.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MyArticleStatsDto {

    private Long approvedCount;
    private Long pendingCount;
    private Long rejectedCount;
    private Long draftCount;
}
