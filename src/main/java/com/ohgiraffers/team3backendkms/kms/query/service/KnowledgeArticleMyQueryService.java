package com.ohgiraffers.team3backendkms.kms.query.service;

import com.ohgiraffers.team3backendkms.kms.query.dto.MyArticleDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.MyArticleHistoryDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.MyArticleStatsDto;
import com.ohgiraffers.team3backendkms.kms.query.dto.request.MyArticleQueryRequest;
import com.ohgiraffers.team3backendkms.kms.query.mapper.KnowledgeArticleMapper;
import com.ohgiraffers.team3backendkms.kms.query.mapper.KnowledgeTagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgeArticleMyQueryService {

    private final KnowledgeArticleMapper knowledgeArticleMapper;
    private final KnowledgeTagMapper knowledgeTagMapper;

    public MyArticleStatsDto getMyArticleStats(Long authorId) {
        return knowledgeArticleMapper.findMyArticleStats(authorId);
    }

    public List<MyArticleDto> getMyArticles(Long authorId, MyArticleQueryRequest request) {
        List<MyArticleDto> articles = knowledgeArticleMapper.findMyArticles(authorId, request);
        articles.forEach(article -> article.setTags(
                knowledgeTagMapper.findTagsByArticleId(article.getArticleId())
        ));
        return articles;
    }

    public List<MyArticleHistoryDto> getMyRecentArticleHistory(Long authorId) {
        return knowledgeArticleMapper.findMyRecentArticleHistory(authorId);
    }
}
