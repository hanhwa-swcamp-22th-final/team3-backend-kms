package com.ohgiraffers.team3backendkms.infrastructure.client;

import com.ohgiraffers.team3backendkms.infrastructure.client.dto.HrTierCriteriaItem;
import java.util.List;

public interface HrClient {

    List<HrTierCriteriaItem> getTierCriteria();
}
