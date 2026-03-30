package com.ohgiraffers.team3backendkms.kms.command.domain.repository;

import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.ArticleCategory;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.ArticleStatus;
import com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.KnowledgeArticle;
import com.ohgiraffers.team3backendkms.common.idgenerator.TimeBasedIdGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("KnowledgeArticleRepository")
class KnowledgeArticleRepositoryTest {

    @Autowired
    private KnowledgeArticleRepository knowledgeArticleRepository;

    private final TimeBasedIdGenerator idGenerator = new TimeBasedIdGenerator();

    private KnowledgeArticle buildArticle(ArticleStatus status) {
        return KnowledgeArticle.builder()
                .articleId(idGenerator.generate())
                .authorId(1L)
                .equipmentId(1L)
                .articleTitle("테스트 지식 문서 제목입니다")
                .articleCategory(ArticleCategory.TROUBLESHOOTING)
                .articleContent("테스트 본문 내용입니다. 최소 50자 이상이어야 합니다. 충분한 내용을 작성합니다.")
                .articleStatus(status)
                .isDeleted(false)
                .viewCount(0)
                .build();
    }

    // =========================================================
    // save()
    // =========================================================

    @Nested
    // 지식 문서 저장 (save)
    @DisplayName("save()")
    class SaveTest {

        @Test
        // 저장하면 ID로 조회할 수 있다
        @DisplayName("Saves article and can be found by ID")
        void save_PersistsId() {
            // given
            KnowledgeArticle article = buildArticle(ArticleStatus.PENDING);
            Long savedId = knowledgeArticleRepository.save(article).getArticleId();

            // when
            Optional<KnowledgeArticle> result = knowledgeArticleRepository.findById(savedId);

            // then
            assertTrue(result.isPresent());
            assertEquals(savedId, result.get().getArticleId());
        }

        @Test
        // 저장한 문서의 필드가 DB에 그대로 저장된다
        @DisplayName("Persists all fields in DB")
        void save_PersistsFields() {
            // given
            KnowledgeArticle article = buildArticle(ArticleStatus.PENDING);
            Long savedId = knowledgeArticleRepository.save(article).getArticleId();

            // when
            KnowledgeArticle found = knowledgeArticleRepository.findById(savedId).orElseThrow();

            // then
            assertEquals(ArticleStatus.PENDING, found.getArticleStatus());
            assertEquals("테스트 지식 문서 제목입니다", found.getArticleTitle());
            assertEquals(ArticleCategory.TROUBLESHOOTING, found.getArticleCategory());
            assertFalse(found.getIsDeleted());
        }
    }

    // =========================================================
    // findById()
    // =========================================================

    @Nested
    // 지식 문서 단건 조회 (findById)
    @DisplayName("findById()")
    class FindByIdTest {

        @Test
        // 저장된 문서를 ID로 조회하면 정상 반환된다
        @DisplayName("Returns saved article by ID")
        void findById_ReturnsSavedArticle() {
            // given
            KnowledgeArticle saved = knowledgeArticleRepository.save(buildArticle(ArticleStatus.DRAFT));

            // when
            Optional<KnowledgeArticle> result = knowledgeArticleRepository.findById(saved.getArticleId());

            // then
            assertTrue(result.isPresent());
            assertEquals(saved.getArticleId(), result.get().getArticleId());
        }

        @Test
        // 존재하지 않는 ID로 조회하면 empty가 반환된다
        @DisplayName("Returns empty when ID does not exist")
        void findById_ReturnsEmpty_WhenNotFound() {
            // given
            Long notExistId = 999L;

            // when
            Optional<KnowledgeArticle> result = knowledgeArticleRepository.findById(notExistId);

            // then
            assertTrue(result.isEmpty());
        }
    }

    // =========================================================
    // delete()
    // =========================================================

    @Nested
    // 지식 문서 삭제 (delete)
    @DisplayName("delete()")
    class DeleteTest {

        @Test
        // 삭제 후 조회하면 empty가 반환된다
        @DisplayName("Returns empty after deletion")
        void delete_ThenFindById_ReturnsEmpty() {
            // given
            KnowledgeArticle saved = knowledgeArticleRepository.save(buildArticle(ArticleStatus.DRAFT));
            Long id = saved.getArticleId();

            // when
            knowledgeArticleRepository.deleteById(id);

            // then
            assertTrue(knowledgeArticleRepository.findById(id).isEmpty());
        }
    }
}
