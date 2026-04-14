package com.ohgiraffers.team3backendkms.infrastructure.client;

import com.ohgiraffers.team3backendkms.infrastructure.client.dto.ExternalApiResponse;
import com.ohgiraffers.team3backendkms.infrastructure.client.dto.HrTierCriteriaItem;
import com.ohgiraffers.team3backendkms.infrastructure.client.feign.HrFeignApi;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "feign.hr", name = "url")
public class HrFeignClient implements HrClient {

    private final HrFeignApi hrFeignApi;

    @Override
    public List<HrTierCriteriaItem> getTierCriteria() {
        ExternalApiResponse<List<HrTierCriteriaItem>> response = hrFeignApi.getTierCriteria();
        return response != null && response.getData() != null ? response.getData() : List.of();
    }
}
