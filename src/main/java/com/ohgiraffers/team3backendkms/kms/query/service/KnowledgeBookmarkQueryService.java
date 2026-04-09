package com.ohgiraffers.team3backendkms.kms.query.service;

import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleReadDto;
import com.ohgiraffers.team3backendkms.kms.query.mapper.KnowledgeBookmarkMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgeBookmarkQueryService {

    private final KnowledgeBookmarkMapper bookmarkMapper;

    public List<ArticleReadDto> getMyBookmarks(Long employeeId) {
        return bookmarkMapper.findBookmarksByEmployeeId(employeeId);
    }
}
