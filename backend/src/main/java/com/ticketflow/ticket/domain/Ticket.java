package com.ticketflow.ticket.domain;

import com.ticketflow.common.entity.BaseTimeEntity;
import com.ticketflow.common.exception.BusinessException;
import com.ticketflow.common.exception.ErrorCode;
import com.ticketflow.common.exception.InvalidTransitionException;
import com.ticketflow.company.domain.Company;
import com.ticketflow.user.domain.Role;
import com.ticketflow.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tickets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ticket extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TicketStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Priority priority;

    @Builder
    private Ticket(User requester, String title, String description, Priority priority) {
        this.requester = requester;
        this.company = requester.getCompany();
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = TicketStatus.OPEN;
    }

    public void changeStatus(TicketStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new InvalidTransitionException(status + " → " + target + " 전이는 허용되지 않습니다.");
        }
        this.status = target;
    }

    public void assignTo(User assignee) {
        if (assignee == null || assignee.getRole() != Role.ADMIN) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "담당자는 같은 회사의 ADMIN 만 가능합니다.");
        }
        if (!assignee.getCompany().getId().equals(this.company.getId())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "다른 회사 사용자에게 배정할 수 없습니다.");
        }
        if (this.status == TicketStatus.RESOLVED || this.status == TicketStatus.CLOSED) {
            throw new InvalidTransitionException("해결/종료된 티켓은 담당자를 배정할 수 없습니다.");
        }
        this.assignee = assignee;
    }
}
