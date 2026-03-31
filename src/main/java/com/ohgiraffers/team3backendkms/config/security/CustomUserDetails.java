package com.ohgiraffers.team3backendkms.config.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CustomUserDetails extends User {

    private final Long employeeId;

    public CustomUserDetails(String username, String password,
                             Collection<? extends GrantedAuthority> authorities,
                             Long employeeId) {
      // 기존에는 코드,비번,권한만 있었는데, 레코드에 long타입 직원번호가 필요해서 추가
        super(username, password, authorities);
        this.employeeId = employeeId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }
}
