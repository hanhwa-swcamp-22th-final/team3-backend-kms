package com.ohgiraffers.team3backendkms.auth.command.application.service;

import com.ohgiraffers.team3backendkms.auth.command.domain.aggregate.Employee;
import com.ohgiraffers.team3backendkms.auth.command.domain.repository.EmployeeRepository;
import com.ohgiraffers.team3backendkms.config.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Employee employee = this.employeeRepository.findByEmployeeCode(username)
                .orElseThrow(() -> new UsernameNotFoundException("회원을 찾을 수 없습니다"));

        // JWT 필터가 토큰에서 직원번호를 꺼넨후 이름호출후 DB조회후 디테일만들어 반환
        return new CustomUserDetails(
                employee.getEmployeeCode(),
                employee.getEmployeePassword(),
                Collections.singleton(new SimpleGrantedAuthority(employee.getEmployeeRole().name())),
                employee.getEmployeeId()
        );
    }

}
