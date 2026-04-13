package com.ohgiraffers.team3backendkms.kms.command.domain.aggregate.employeementoringfield;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "employee_mentoring_field")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class EmployeeMentoringField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long employeeMentoringFieldId;

    @Column(nullable = false)
    private Long employeeId;

    @Column(nullable = false, length = 100)
    private String mentoringField;   // ArticleCategory enum name (VARCHAR(100))

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(updatable = false)
    private Long createdBy;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @LastModifiedBy
    private Long updatedBy;
}
