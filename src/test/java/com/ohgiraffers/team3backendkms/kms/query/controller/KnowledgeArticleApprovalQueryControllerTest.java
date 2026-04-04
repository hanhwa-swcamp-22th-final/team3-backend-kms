package com.ohgiraffers.team3backendkms.kms.query.controller;

import com.ohgiraffers.team3backendkms.common.exception.GlobalExceptionHandler;
import com.ohgiraffers.team3backendkms.kms.query.dto.ApprovalArticleDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.ApprovalStatsDto;
import com.ohgiraffers.team3backendkms.kms.query.service.KnowledgeArticleApprovalQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = KnowledgeArticleApprovalQueryController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
@Import(GlobalExceptionHandler.class)
class KnowledgeArticleApprovalQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private KnowledgeArticleApprovalQueryService knowledgeArticleApprovalQueryService;

    @Nested
    @DisplayName("GET /api/kms/approval")
    class GetApprovalArticles {

        @Test
        @DisplayName("Returns approval article list with 200 OK")
        void getApprovalArticles_Success() throws Exception {
            // given
            ApprovalArticleDto dto = new ApprovalArticleDto();
            dto.setArticleId(1L);
            dto.setArticleTitle("승인 대기 문서 제목입니다");

            given(knowledgeArticleApprovalQueryService.getApprovalArticles(any())).willReturn(List.of(dto));

            // when & then
            mockMvc.perform(get("/api/kms/approval"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].articleId").value(1))
                .andExpect(jsonPath("$.data[0].articleTitle").value("승인 대기 문서 제목입니다"));
        }
    }

    @Nested
    @DisplayName("GET /api/kms/approval/stats")
    class GetApprovalStats {

        @Test
        @DisplayName("Returns approval stats with 200 OK")
        void getApprovalStats_Success() throws Exception {
            // given
            ApprovalStatsDto dto = new ApprovalStatsDto();
            dto.setPendingCount(5L);
            dto.setApprovedThisMonth(10L);
            dto.setRejectionRate(33.33);

            given(knowledgeArticleApprovalQueryService.getApprovalStats()).willReturn(dto);

            // when & then
            mockMvc.perform(get("/api/kms/approval/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pendingCount").value(5))
                .andExpect(jsonPath("$.data.approvedThisMonth").value(10))
                .andExpect(jsonPath("$.data.rejectionRate").value(33.33));
        }
    }
}
