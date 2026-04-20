package com.ohgiraffers.team3backendkms.jwt;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "refresh_token")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @Column(name = "employee_code", nullable = false)
    private String employeeCode;

    @Column(name = "token", nullable = false, length = 1000)
    private String token;

    @Column(name = "login_session_id", length = 36)
    private String loginSessionId;

    @Column(name = "expiry_date", nullable = false)
    private Date expiryDate;
}
