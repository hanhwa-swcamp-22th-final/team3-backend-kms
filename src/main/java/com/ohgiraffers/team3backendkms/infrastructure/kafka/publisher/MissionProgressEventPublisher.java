package com.ohgiraffers.team3backendkms.infrastructure.kafka.publisher;

import com.ohgiraffers.team3backendkms.infrastructure.kafka.dto.MissionProgressEvent;
import com.ohgiraffers.team3backendkms.infrastructure.kafka.support.MissionKafkaTopics;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Component
@RequiredArgsConstructor
public class MissionProgressEventPublisher {

    private static final String KMS_CONTRIBUTION = "KMS_CONTRIBUTION";

    private final KafkaTemplate<String, MissionProgressEvent> missionProgressKafkaTemplate;

    public void publishKmsContributionAfterCommit(Long employeeId) {
        MissionProgressEvent event = MissionProgressEvent.builder()
                .employeeId(employeeId)
                .missionType(KMS_CONTRIBUTION)
                .progressValue(BigDecimal.ONE)
                .absolute(false)
                .build();

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publish(event);
                }
            });
            return;
        }

        publish(event);
    }

    private void publish(MissionProgressEvent event) {
        missionProgressKafkaTemplate.send(
                MissionKafkaTopics.MISSION_PROGRESS_UPDATED,
                String.valueOf(event.getEmployeeId()),
                event
        );
        log.info("[Mission] Published KMS contribution event. employeeId={}", event.getEmployeeId());
    }
}
