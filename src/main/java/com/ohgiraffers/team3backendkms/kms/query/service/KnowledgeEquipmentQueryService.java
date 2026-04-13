package com.ohgiraffers.team3backendkms.kms.query.service;

import com.ohgiraffers.team3backendkms.kms.query.dto.EquipmentReadDto;
import com.ohgiraffers.team3backendkms.kms.query.mapper.KnowledgeEquipmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgeEquipmentQueryService {

    private final KnowledgeEquipmentMapper knowledgeEquipmentMapper;

    public List<EquipmentReadDto> getAllEquipments() {
        return knowledgeEquipmentMapper.findAllEquipments();
    }
}
