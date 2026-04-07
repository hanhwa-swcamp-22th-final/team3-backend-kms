package com.ohgiraffers.team3backendkms.kms.query.mapper;

import com.ohgiraffers.team3backendkms.kms.query.dto.KnowledgeTagReadDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface KnowledgeTagMapper {

    List<KnowledgeTagReadDto> findAllTags();
}
