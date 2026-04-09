package com.ohgiraffers.team3backendkms.kms.query.mapper;

import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleReadDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 북마크 MyBatis 매퍼
 * - 읽기 전용 쿼리 담당
 * - XML: resources/mapper/KnowledgeBookmarkMapper.xml
 */
@Mapper
public interface KnowledgeBookmarkMapper {

    // 특정 직원이 북마크한 게시글 목록 조회
    List<ArticleReadDto> findBookmarksByEmployeeId(Long employeeId);
}
