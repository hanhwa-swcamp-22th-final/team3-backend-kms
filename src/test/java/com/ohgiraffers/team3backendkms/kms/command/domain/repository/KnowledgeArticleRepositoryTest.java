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

    @Autowired // di ioc
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
    @DisplayName("지식 문서 저장 (save)")
    class SaveTest {

        @Test
        @DisplayName("저장하면 ID가 그대로 유지된다")
        void save_PersistsId() {
            // given
            KnowledgeArticle article = buildArticle(ArticleStatus.PENDING);

            // when
            KnowledgeArticle saved = knowledgeArticleRepository.save(article);

            // then 적절 x 인서트 셀렉으로 변결ㅇ
            assertNotNull(saved.getArticleId());
        }

        @Test
        @DisplayName("저장한 문서의 필드가 그대로 유지된다")
        void save_PersistsFields() {
            // given
            KnowledgeArticle article = buildArticle(ArticleStatus.PENDING);

            // when
            KnowledgeArticle saved = knowledgeArticleRepository.save(article);

            // then
            assertEquals(ArticleStatus.PENDING, saved.getArticleStatus());
            assertEquals("테스트 지식 문서 제목입니다", saved.getArticleTitle());
            assertEquals(ArticleCategory.TROUBLESHOOTING, saved.getArticleCategory());
            assertFalse(saved.getIsDeleted());
        }
    }

    // =========================================================
    // findById()
    // =========================================================

    @Nested
    @DisplayName("지식 문서 단건 조회 (findById)")
    class FindByIdTest {

        @Test
        @DisplayName("저장된 문서를 ID로 조회하면 정상 반환된다")
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
        @DisplayName("존재하지 않는 ID로 조회하면 empty가 반환된다")
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
    @DisplayName("지식 문서 삭제 (delete)")
    class DeleteTest {

        @Test
        @DisplayName("삭제 후 조회하면 empty가 반환된다")
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
