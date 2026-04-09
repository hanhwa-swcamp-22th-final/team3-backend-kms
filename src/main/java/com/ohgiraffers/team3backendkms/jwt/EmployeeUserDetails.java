package com.ohgiraffers.team3backendkms.jwt;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class EmployeeUserDetails extends User {

    private final Long employeeId;

    public EmployeeUserDetails(Long employeeId, String employeeCode,
                                Collection<? extends GrantedAuthority> authorities) {
        super(employeeCode, "", authorities);
        this.employeeId = employeeId;
    }

    /** @deprecated password is not used in JWT-only authentication. Use 3-param constructor. */
    @Deprecated
    public EmployeeUserDetails(Long employeeId, String employeeCode, String password,
                                Collection<? extends GrantedAuthority> authorities) {
        this(employeeId, employeeCode, authorities);
    }

}
