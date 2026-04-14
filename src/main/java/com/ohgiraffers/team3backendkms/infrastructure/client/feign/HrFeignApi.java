package com.ohgiraffers.team3backendkms.infrastructure.client.feign;

import com.ohgiraffers.team3backendkms.infrastructure.client.dto.ExternalApiResponse;
import com.ohgiraffers.team3backendkms.infrastructure.client.dto.HrTierCriteriaItem;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "kmsHrFeignApi",
        url = "${feign.hr.url}",
        configuration = ExternalAuthorizationForwardingConfiguration.class
)
public interface HrFeignApi {

    @GetMapping("/api/v1/hr/evaluation/criteria")
    ExternalApiResponse<List<HrTierCriteriaItem>> getTierCriteria();
}
