package com.ohgiraffers.team3backendkms.jwt;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthRepository extends JpaRepository<RefreshToken, String> {
}
