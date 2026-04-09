package com.ohgiraffers.team3backendkms.kms.query.mapper;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.ArticleStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticle.KnowledgeArticle;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticletag.KnowledgeArticleTag;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgearticletag.KnowledgeArticleTagId;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.knowledgetag.KnowledgeTag;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeArticleRepository;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeArticleTagRepository;
import com.ohgiraffers.team3backendkms.kms.command.domain.repository.KnowledgeTagRepository;
import com.ohgiraffers.team3backendkms.kms.query.dto.KnowledgeTagReadDto;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.AutoConfigureMybatis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@AutoConfigureMybatis
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class KnowledgeTagMapperTest {

    private static final Long TEST_ARTICLE_ID = 9000000002001L;
    private static final Long TEST_EQUIPMENT_ID = 9000000097L;
    private static final Long TEST_TAG_ID_1 = 9000000003001L;
    private static final Long TEST_TAG_ID_2 = 9000000003002L;

    @Autowired
    private KnowledgeTagMapper knowledgeTagMapper;

    @Autowired
    private KnowledgeArticleRepository knowledgeArticleRepository;

    @Autowired
    private KnowledgeTagRepository knowledgeTagRepository;

    @Autowired
    private KnowledgeArticleTagRepository knowledgeArticleTagRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    private Long authorId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=0");
        jdbcTemplate.execute(
                "INSERT IGNORE INTO attachment_file_group (file_group_id, reference_type) VALUES (0, 'KNOWLEDGE')"
        );
        jdbcTemplate.execute(
                "INSERT IGNORE INTO equipment " +
                        "(equipment_id, equipment_process_id, environment_standard_id, equipment_code, equipment_name, equipment_status, equipment_grade) " +
                        "VALUES (" + TEST_EQUIPMENT_ID + ", 1, 1, 'TEST-TAG-MAPPER', '태그매퍼테스트 설비', 'OPERATING', 'A')"
        );
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=1");

        authorId = jdbcTemplate.queryForObject("SELECT employee_id FROM employee LIMIT 1", Long.class);

        knowledgeArticleTagRepository.deleteByIdArticleId(TEST_ARTICLE_ID);
        knowledgeTagRepository.deleteAllById(List.of(TEST_TAG_ID_1, TEST_TAG_ID_2));
        if (knowledgeArticleRepository.existsById(TEST_ARTICLE_ID)) {
            knowledgeArticleRepository.deleteById(TEST_ARTICLE_ID);
        }

        knowledgeArticleRepository.save(KnowledgeArticle.builder()
                .articleId(TEST_ARTICLE_ID)
                .authorId(authorId)
                .equipmentId(TEST_EQUIPMENT_ID)
                .fileGroupId(0L)
                .articleTitle("태그 조회 테스트 문서")
                .articleCategory(ArticleCategory.TROUBLESHOOTING)
                .articleContent("태그 조회 테스트용 본문입니다.")
                .articleStatus(ArticleStatus.APPROVED)
                .approvalVersion(0)
                .isDeleted(false)
                .viewCount(0)
                .createdAt(LocalDateTime.now())
                .build());

        knowledgeTagRepository.saveAll(List.of(
                KnowledgeTag.builder().tagId(TEST_TAG_ID_1).tagName("가공").build(),
                KnowledgeTag.builder().tagId(TEST_TAG_ID_2).tagName("설비").build()
        ));

        knowledgeArticleTagRepository.saveAll(List.of(
                KnowledgeArticleTag.builder().id(new KnowledgeArticleTagId(TEST_TAG_ID_1, TEST_ARTICLE_ID)).build(),
                KnowledgeArticleTag.builder().id(new KnowledgeArticleTagId(TEST_TAG_ID_2, TEST_ARTICLE_ID)).build()
        ));

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("findTagsByArticleId returns tags linked to article")
    void findTagsByArticleId_success() {
        List<KnowledgeTagReadDto> result = knowledgeTagMapper.findTagsByArticleId(TEST_ARTICLE_ID);

        assertNotNull(result);
        Set<String> tagNames = result.stream()
                .map(KnowledgeTagReadDto::getTagName)
                .collect(java.util.stream.Collectors.toSet());
        org.junit.jupiter.api.Assertions.assertTrue(tagNames.contains("가공"));
        org.junit.jupiter.api.Assertions.assertTrue(tagNames.contains("설비"));
    }
}
