package com.ohgiraffers.team3backendkms.kms.query.service;

import com.ohgiraffers.team3backendkms.kms.query.dto.KnowledgeTagReadDto;
import com.ohgiraffers.team3backendkms.kms.query.mapper.KnowledgeTagMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgeTagQueryService {

    private final KnowledgeTagMapper knowledgeTagMapper;

    public List<KnowledgeTagReadDto> getAllTags() {
        return knowledgeTagMapper.findAllTags();
    }
}
