package com.ticketflow.user.domain;

import com.ticketflow.common.entity.BaseCreatedEntity;
import com.ticketflow.company.domain.Company;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 회사 소속 사용자. 비밀번호는 BCrypt 해시로 저장한다. (스키마는 Flyway 소유) */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Role role;

    @Builder
    private User(Company company, String username, String passwordHash, String name, Role role) {
        this.company = company;
        this.username = username;
        this.passwordHash = passwordHash;
        this.name = name;
        this.role = role;
    }
}
