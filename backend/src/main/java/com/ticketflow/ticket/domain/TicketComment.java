package com.ticketflow.ticket.domain;

import com.ticketflow.common.entity.BaseCreatedEntity;
import com.ticketflow.common.exception.BusinessException;
import com.ticketflow.common.exception.ErrorCode;
import com.ticketflow.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ticket_comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TicketComment extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 1000)
    private String message;

    @Builder
    private TicketComment(Ticket ticket, User author, String message) {
        if (!author.getCompany().getId().equals(ticket.getCompany().getId())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "다른 회사 사용자는 댓글을 달 수 없습니다.");
        }
        this.ticket = ticket;
        this.author = author;
        this.message = message;
    }
}
