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

    public Long getApproved() {
        return approvedCount;
    }

    public Long getPending() {
        return pendingCount;
    }

    public Long getRejected() {
        return rejectedCount;
    }

    public Long getDraft() {
        return draftCount;
    }

    public Long getTotal() {
        return safe(approvedCount) + safe(pendingCount) + safe(rejectedCount) + safe(draftCount);
    }

    private long safe(Long value) {
        return value == null ? 0L : value;
    }
}
