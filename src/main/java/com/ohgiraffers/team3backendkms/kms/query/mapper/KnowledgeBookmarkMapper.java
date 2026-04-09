package com.ohgiraffers.team3backendkms.kms.query.mapper;

import com.ohgiraffers.team3backendkms.kms.query.dto.ArticleReadDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface KnowledgeBookmarkMapper {

    List<ArticleReadDto> findBookmarksByEmployeeId(Long employeeId);
}
