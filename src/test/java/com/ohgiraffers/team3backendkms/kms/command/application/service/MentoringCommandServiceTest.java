package com.ohgiraffers.team3backendkms.kms.command.application.service;

import com.ohgiraffers.team3backendkms.common.exception.BusinessException;
import com.ohgiraffers.team3backendkms.common.exception.MentoringErrorCode;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.KnowledgeArticle;
import com.ohgiraffers.team3backendkms.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoringemployee.EmployeeStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoringemployee.EmployeeTier;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoringemployee.MentoringEmployee;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoringemployee.MentoringEmployeeRole;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoringrequest.MentoringRequest;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoringrequest.MentoringRequestStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoringrequest.RequestPriority;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeArticleRepository;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.MentoringEmployeeRepository;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.MentoringRequestRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MentoringCommandServiceTest {

    @InjectMocks
    private MentoringCommandService mentoringCommandService;

    @Mock
    private MentoringRequestRepository mentoringRequestRepository;

    @Mock
    private MentoringEmployeeRepository mentoringEmployeeRepository;

    @Mock
    private KnowledgeArticleRepository knowledgeArticleRepository;

    @Mock
    private IdGenerator idGenerator;

    @Nested
    @DisplayName("createRequest()")
    class CreateRequest {

        @Test
        @DisplayName("Saves mentoring request with PENDING status")
        void createRequest_success() {
            MentoringEmployee mentee = MentoringEmployee.builder()
                    .employeeId(1L)
                    .employeeRole(MentoringEmployeeRole.WORKER)
                    .employeeTier(EmployeeTier.B)
                    .employeeStatus(EmployeeStatus.ACTIVE)
                    .build();
            KnowledgeArticle article = KnowledgeArticle.builder()
                    .articleId(11L)
                    .isDeleted(false)
                    .build();

            given(mentoringEmployeeRepository.findById(1L)).willReturn(Optional.of(mentee));
            given(knowledgeArticleRepository.findById(11L)).willReturn(Optional.of(article));
            given(mentoringRequestRepository.existsByMenteeIdAndMentoringFieldAndArticleIdAndRequestStatusIn(anyLong(), anyString(), anyLong(), anyCollection()))
                    .willReturn(false);
            given(idGenerator.generate()).willReturn(100L);
            given(mentoringRequestRepository.save(any(MentoringRequest.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            Long requestId = mentoringCommandService.createRequest(
                    1L,
                    11L,
                    "설비보전",
                    "설비보전 멘토링 요청",
                    "설비보전 기준과 고장 대응 순서에 대한 멘토링을 받고 싶습니다.",
                    4,
                    "주 2회",
                    RequestPriority.HIGH
            );

            ArgumentCaptor<MentoringRequest> captor = ArgumentCaptor.forClass(MentoringRequest.class);
            verify(mentoringRequestRepository).save(captor.capture());
            assertEquals(100L, requestId);
            assertEquals(MentoringRequestStatus.PENDING, captor.getValue().getRequestStatus());
            assertEquals(1L, captor.getValue().getMenteeId());
            assertEquals("설비보전", captor.getValue().getMentoringField());
        }

        @Test
        @DisplayName("Throws exception when requester is not B/C worker")
        void createRequest_whenRequesterIsNotEligible_thenThrowException() {
            MentoringEmployee mentee = MentoringEmployee.builder()
                    .employeeId(1L)
                    .employeeRole(MentoringEmployeeRole.WORKER)
                    .employeeTier(EmployeeTier.A)
                    .employeeStatus(EmployeeStatus.ACTIVE)
                    .build();

            given(mentoringEmployeeRepository.findById(1L)).willReturn(Optional.of(mentee));

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    mentoringCommandService.createRequest(
                            1L,
                            null,
                            "설비보전",
                            "설비보전 멘토링 요청",
                            "설비보전 기준과 고장 대응 순서에 대한 멘토링을 받고 싶습니다.",
                            4,
                            "주 2회",
                            RequestPriority.HIGH
                    )
            );

            assertEquals(MentoringErrorCode.MENTORING_REQUEST_001, exception.getErrorCode());
        }
    }
}
