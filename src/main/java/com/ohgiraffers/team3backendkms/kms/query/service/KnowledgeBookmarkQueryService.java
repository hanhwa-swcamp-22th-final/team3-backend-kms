package com.ohgiraffers.team3backendkms.kms.query.service;

import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleReadDto;
import com.ohgiraffers.team3backendkms.kms.query.mapper.KnowledgeBookmarkMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 북마크 Query 서비스 — 북마크 목록 조회
 * 읽기 전용 (쓰기는 KnowledgeBookmarkCommandService)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgeBookmarkQueryService {

    private final KnowledgeBookmarkMapper bookmarkMapper;

    // 내 북마크 목록 조회 — 삭제된 게시글 제외, 북마크 등록일 최신순
    public List<ArticleReadDto> getMyBookmarks(Long employeeId) {
        return bookmarkMapper.findBookmarksByEmployeeId(employeeId);
    }
}
