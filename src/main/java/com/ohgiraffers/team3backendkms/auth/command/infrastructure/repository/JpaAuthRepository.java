package com.ohgiraffers.team3backendkms.auth.command.infrastructure.repository;

import com.ohgiraffers.team3backendkms.auth.command.domain.aggregate.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaAuthRepository extends JpaRepository<RefreshToken, String> {

}
