package com.ohgiraffers.team3backendkms.kms.query.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.ohgiraffers.team3backendkms.kms.query.service.dto.SkillGapAiReviewRequest;
import com.ohgiraffers.team3backendkms.kms.query.service.dto.SkillGapAiReviewResult;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Service
public class SkillGapAiReviewService {

    private static final String VERTEX_AI_ENDPOINT =
            "https://%s-aiplatform.googleapis.com/v1/projects/%s/locations/%s/publishers/google/models/%s:generateContent";
    private static final String GLOBAL_VERTEX_AI_ENDPOINT =
            "https://aiplatform.googleapis.com/v1/projects/%s/locations/global/publishers/google/models/%s:generateContent";
    private static final String CLOUD_PLATFORM_SCOPE = "https://www.googleapis.com/auth/cloud-platform";

    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;

    @Value("${google.ai.credentials-location:${nlp.google.credentials-location:}}")
    private String credentialsLocation;

    @Value("${google.ai.project-id:}")
    private String configuredProjectId;

    @Value("${google.ai.location:global}")
    private String googleAiLocation;

    @Value("${google.ai.model:gemini-3-flash-preview}")
    private String googleAiModel;

    public SkillGapAiReviewService(ObjectMapper objectMapper, ResourceLoader resourceLoader) {
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
    }

    public SkillGapAiReviewResult review(SkillGapAiReviewRequest request) {
        if (!StringUtils.hasText(credentialsLocation) || request == null || request.getTopGaps() == null
                || request.getTopGaps().isEmpty()) {
            return SkillGapAiReviewResult.disabled();
        }

        try {
            GoogleCredentials credentials = loadCredentials();
            String projectId = resolveProjectId(credentials);
            AccessToken token = credentials.refreshAccessToken();
            String endpoint = buildVertexAiEndpoint(projectId);

            JsonNode response = RestClient.create()
                    .post()
                    .uri(endpoint)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getTokenValue())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(buildVertexAiRequest(request))
                    .retrieve()
                    .body(JsonNode.class);

            return parseResponse(response);
        } catch (Exception exception) {
            return SkillGapAiReviewResult.disabled();
        }
    }

    private GoogleCredentials loadCredentials() throws Exception {
        Resource resource = resolveResource(credentialsLocation);
        try (InputStream inputStream = resource.getInputStream()) {
            return GoogleCredentials.fromStream(inputStream)
                    .createScoped(List.of(CLOUD_PLATFORM_SCOPE));
        }
    }

    private String resolveProjectId(GoogleCredentials credentials) {
        if (StringUtils.hasText(configuredProjectId)) {
            return configuredProjectId;
        }
        if (credentials instanceof ServiceAccountCredentials serviceAccountCredentials
                && StringUtils.hasText(serviceAccountCredentials.getProjectId())) {
            return serviceAccountCredentials.getProjectId();
        }
        throw new IllegalStateException("Google AI project id를 확인할 수 없습니다.");
    }

    private String buildVertexAiEndpoint(String projectId) {
        if ("global".equalsIgnoreCase(googleAiLocation)) {
            return GLOBAL_VERTEX_AI_ENDPOINT.formatted(projectId, googleAiModel);
        }
        return VERTEX_AI_ENDPOINT.formatted(
                googleAiLocation,
                projectId,
                googleAiLocation,
                googleAiModel
        );
    }

    private Resource resolveResource(String location) {
        if (location.startsWith("classpath:") || location.startsWith("file:")) {
            return resourceLoader.getResource(location);
        }
        return resourceLoader.getResource("file:" + location);
    }

    private Map<String, Object> buildVertexAiRequest(SkillGapAiReviewRequest request) {
        return Map.of(
                "contents",
                List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", buildPrompt(request)))
                )),
                "generationConfig",
                Map.of(
                        "temperature", 0.2,
                        "topP", 0.8,
                        "maxOutputTokens", 2048,
                        "responseMimeType", "application/json"
                )
        );
    }

    private String buildPrompt(SkillGapAiReviewRequest request) {
        return """
                당신은 반도체 제조 현장의 작업자 역량 성장 코치를 돕는 한국어 Skill Gap 분석가입니다.
                입력 데이터는 이미 시스템이 계산한 결과이며, 점수나 티어를 다시 계산하거나 변경하면 안 됩니다.

                반드시 지켜야 할 규칙:
                1. 모든 답변은 한국어로 작성합니다.
                2. JSON 한 개만 반환합니다. 코드블록, 마크다운, 설명 문장은 절대 추가하지 않습니다.
                3. summary는 2문장 이내로 작성합니다.
                4. gapRecommendations는 입력 topGaps에 포함된 skillName만 사용합니다.
                5. recommendation은 각 역량별로 1문장만 작성합니다.
                6. 막연한 표현보다 현재 점수, 목표 점수, gap 크기를 바탕으로 우선순위를 분명히 설명합니다.
                7. 교육 과정명이나 제도를 지어내지 말고, 현장 학습/문서 학습/반복 실습 수준에서 제안합니다.

                반환 JSON 형식:
                {
                  "summary": "문자열",
                  "gapRecommendations": [
                    {
                      "skillName": "역량명",
                      "recommendation": "추천 문장"
                    }
                  ]
                }

                입력 데이터:
                %s
                """.formatted(toPrettyJson(request));
    }

    private SkillGapAiReviewResult parseResponse(JsonNode response) {
        String text = extractResponseText(response);
        if (!StringUtils.hasText(text)) {
            return SkillGapAiReviewResult.disabled();
        }

        try {
            JsonNode root = objectMapper.readTree(extractJsonObject(text));
            String summary = root.path("summary").asText("").trim();

            Map<String, String> recommendations = new LinkedHashMap<>();
            JsonNode recommendationNodes = root.path("gapRecommendations");
            if (recommendationNodes.isArray()) {
                for (JsonNode recommendationNode : recommendationNodes) {
                    String skillName = recommendationNode.path("skillName").asText("").trim();
                    String recommendation = recommendationNode.path("recommendation").asText("").trim();
                    if (StringUtils.hasText(skillName) && StringUtils.hasText(recommendation)) {
                        recommendations.put(skillName, recommendation);
                    }
                }
            }

            if (!StringUtils.hasText(summary) && recommendations.isEmpty()) {
                return SkillGapAiReviewResult.disabled();
            }

            return SkillGapAiReviewResult.builder()
                    .aiEnabled(true)
                    .summary(summary)
                    .gapRecommendations(recommendations)
                    .build();
        } catch (Exception exception) {
            return SkillGapAiReviewResult.disabled();
        }
    }

    private String extractResponseText(JsonNode response) {
        if (response == null) {
            return "";
        }

        JsonNode parts = response.at("/candidates/0/content/parts");
        StringBuilder builder = new StringBuilder();
        if (parts.isArray()) {
            for (JsonNode part : parts) {
                String text = part.path("text").asText("");
                if (StringUtils.hasText(text)) {
                    builder.append(text.trim());
                }
            }
        }
        return builder.toString().trim();
    }

    private String extractJsonObject(String text) {
        String trimmed = text == null ? "" : text.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replace("```json", "").replace("```", "").trim();
        }

        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end >= start) {
            return trimmed.substring(start, end + 1);
        }
        return trimmed;
    }

    private String toPrettyJson(SkillGapAiReviewRequest request) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request);
        } catch (Exception exception) {
            return "{}";
        }
    }
}
