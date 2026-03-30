package com.ohgiraffers.team3backendkms.auth.command.domain.aggregate;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "department")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "parent_department_id", nullable = false)
    private Long parentDepartmentId;

    @Column(name = "department_name", length = 30)
    private String departmentName;

    @Column(name = "team_name")
    private String teamName;

    @Column(name = "depth")
    private String depth;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;
}
