package com.ohgiraffers.team3backendkms.infrastructure.kafka.publisher;

import com.ohgiraffers.team3backendkms.infrastructure.kafka.dto.KmsArticleSnapshotEvent;
import com.ohgiraffers.team3backendkms.infrastructure.kafka.support.KmsKafkaTopics;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KmsArticleSnapshotEventPublisher {

    private final KafkaTemplate<String, KmsArticleSnapshotEvent> kmsArticleSnapshotKafkaTemplate;

    public void publish(KmsArticleSnapshotEvent event) {
        kmsArticleSnapshotKafkaTemplate.send(
            KmsKafkaTopics.KMS_ARTICLE_SNAPSHOT,
            String.valueOf(event.getArticleId()),
            event
        );
    }
}
