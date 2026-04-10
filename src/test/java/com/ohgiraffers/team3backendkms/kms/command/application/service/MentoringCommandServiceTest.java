package com.ohgiraffers.team3backendkms.kms.command.application.service;

import com.ohgiraffers.team3backendkms.common.exception.BusinessException;
import com.ohgiraffers.team3backendkms.common.exception.MentoringErrorCode;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.KnowledgeArticle;
import com.ohgiraffers.team3backendkms.common.idgenerator.IdGenerator;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoring.Mentoring;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoring.MentoringStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoringemployee.EmployeeStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoringemployee.EmployeeTier;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoringemployee.MentoringEmployee;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoringemployee.MentoringEmployeeRole;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoringrequest.MentoringRequest;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoringrequest.MentoringRequestStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.mentoringrequest.RequestPriority;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.EmployeeMentoringFieldRepository;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeArticleRepository;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.MentoringEmployeeRepository;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.MentoringRepository;
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
import static org.mockito.BDDMockito.willDoNothing;
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
    private EmployeeMentoringFieldRepository employeeMentoringFieldRepository;

    @Mock
    private MentoringRepository mentoringRepository;

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

    @Nested
    @DisplayName("acceptRequest()")
    class AcceptRequest {

        @Test
        @DisplayName("Accepts request and creates mentoring with IN_PROGRESS status")
        void acceptRequest_success() {
            MentoringRequest mentoringRequest = MentoringRequest.builder()
                    .requestId(10L)
                    .menteeId(1L)
                    .articleId(11L)
                    .mentoringField("설비보전")
                    .requestTitle("설비보전 멘토링 요청")
                    .requestContent("설비보전 기준과 고장 대응 순서에 대한 멘토링을 받고 싶습니다.")
                    .requestStatus(MentoringRequestStatus.PENDING)
                    .build();
            MentoringEmployee mentor = MentoringEmployee.builder()
                    .employeeId(2L)
                    .employeeRole(MentoringEmployeeRole.WORKER)
                    .employeeTier(EmployeeTier.A)
                    .employeeStatus(EmployeeStatus.ACTIVE)
                    .build();

            given(mentoringRequestRepository.findById(10L)).willReturn(Optional.of(mentoringRequest));
            given(mentoringEmployeeRepository.findById(2L)).willReturn(Optional.of(mentor));
            given(employeeMentoringFieldRepository.existsByEmployeeIdAndMentoringField(2L, "설비보전")).willReturn(true);
            given(mentoringRepository.existsByRequestId(10L)).willReturn(false);
            given(idGenerator.generate()).willReturn(200L);
            given(mentoringRepository.save(any(Mentoring.class))).willAnswer(invocation -> invocation.getArgument(0));

            Long mentoringId = mentoringCommandService.acceptRequest(10L, 2L);

            ArgumentCaptor<Mentoring> captor = ArgumentCaptor.forClass(Mentoring.class);
            verify(mentoringRepository).save(captor.capture());
            assertEquals(200L, mentoringId);
            assertEquals(MentoringStatus.IN_PROGRESS, captor.getValue().getMentoringStatus());
            assertEquals(MentoringRequestStatus.ACCEPTED, mentoringRequest.getRequestStatus());
        }

        @Test
        @DisplayName("Throws exception when mentor is not eligible for field")
        void acceptRequest_whenMentorIsNotEligible_thenThrowException() {
            MentoringRequest mentoringRequest = MentoringRequest.builder()
                    .requestId(10L)
                    .menteeId(1L)
                    .articleId(11L)
                    .mentoringField("설비보전")
                    .requestTitle("설비보전 멘토링 요청")
                    .requestContent("설비보전 기준과 고장 대응 순서에 대한 멘토링을 받고 싶습니다.")
                    .requestStatus(MentoringRequestStatus.PENDING)
                    .build();
            MentoringEmployee mentor = MentoringEmployee.builder()
                    .employeeId(2L)
                    .employeeRole(MentoringEmployeeRole.WORKER)
                    .employeeTier(EmployeeTier.B)
                    .employeeStatus(EmployeeStatus.ACTIVE)
                    .build();

            given(mentoringRequestRepository.findById(10L)).willReturn(Optional.of(mentoringRequest));
            given(mentoringEmployeeRepository.findById(2L)).willReturn(Optional.of(mentor));

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    mentoringCommandService.acceptRequest(10L, 2L)
            );

            assertEquals(MentoringErrorCode.MENTORING_REQUEST_008, exception.getErrorCode());
        }
    }

    @Nested
    @DisplayName("rejectRequest()")
    class RejectRequest {

        @Test
        @DisplayName("Adds mentor to rejected mentor ids and keeps request PENDING")
        void rejectRequest_success() {
            MentoringRequest mentoringRequest = MentoringRequest.builder()
                    .requestId(10L)
                    .menteeId(1L)
                    .articleId(11L)
                    .mentoringField("설비보전")
                    .requestTitle("설비보전 멘토링 요청")
                    .requestContent("설비보전 기준과 고장 대응 순서에 대한 멘토링을 받고 싶습니다.")
                    .requestStatus(MentoringRequestStatus.PENDING)
                    .rejectedMentorIds(null)
                    .build();
            MentoringEmployee mentor = MentoringEmployee.builder()
                    .employeeId(2L)
                    .employeeRole(MentoringEmployeeRole.TL)
                    .employeeTier(EmployeeTier.A)
                    .employeeStatus(EmployeeStatus.ACTIVE)
                    .build();

            given(mentoringRequestRepository.findById(10L)).willReturn(Optional.of(mentoringRequest));
            given(mentoringEmployeeRepository.findById(2L)).willReturn(Optional.of(mentor));
            given(employeeMentoringFieldRepository.existsByEmployeeIdAndMentoringField(2L, "설비보전")).willReturn(true);

            mentoringCommandService.rejectRequest(10L, 2L);

            assertEquals(MentoringRequestStatus.PENDING, mentoringRequest.getRequestStatus());
            assertEquals("[2]", mentoringRequest.getRejectedMentorIds());
        }

        @Test
        @DisplayName("Throws exception when same mentor rejects twice")
        void rejectRequest_whenMentorAlreadyRejected_thenThrowException() {
            MentoringRequest mentoringRequest = MentoringRequest.builder()
                    .requestId(10L)
                    .menteeId(1L)
                    .articleId(11L)
                    .mentoringField("설비보전")
                    .requestTitle("설비보전 멘토링 요청")
                    .requestContent("설비보전 기준과 고장 대응 순서에 대한 멘토링을 받고 싶습니다.")
                    .requestStatus(MentoringRequestStatus.PENDING)
                    .rejectedMentorIds("[2]")
                    .build();
            MentoringEmployee mentor = MentoringEmployee.builder()
                    .employeeId(2L)
                    .employeeRole(MentoringEmployeeRole.DL)
                    .employeeTier(EmployeeTier.S)
                    .employeeStatus(EmployeeStatus.ACTIVE)
                    .build();

            given(mentoringRequestRepository.findById(10L)).willReturn(Optional.of(mentoringRequest));
            given(mentoringEmployeeRepository.findById(2L)).willReturn(Optional.of(mentor));
            given(employeeMentoringFieldRepository.existsByEmployeeIdAndMentoringField(2L, "설비보전")).willReturn(true);

            BusinessException exception = assertThrows(BusinessException.class, () ->
                    mentoringCommandService.rejectRequest(10L, 2L)
            );

            assertEquals(MentoringErrorCode.MENTORING_REQUEST_012, exception.getErrorCode());
        }
    }
}
