package com.ticketflow.ticket.service;

import com.ticketflow.common.exception.NotFoundException;
import com.ticketflow.common.security.AuthUser;
import com.ticketflow.ticket.domain.Ticket;
import com.ticketflow.ticket.domain.TicketComment;
import com.ticketflow.ticket.domain.TicketStatus;
import com.ticketflow.ticket.dto.*;
import com.ticketflow.ticket.repository.TicketCommentRepository;
import com.ticketflow.ticket.repository.TicketRepository;
import com.ticketflow.user.domain.User;
import com.ticketflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketCommentRepository commentRepository;
    private final UserRepository userRepository;

    @Transactional
    public TicketDetail create(AuthUser me, CreateTicketRequest req) {
        User requester = userRepository.findByIdAndCompanyId(me.userId(), me.companyId())
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
        Ticket ticket = ticketRepository.save(Ticket.builder()
                .requester(requester)
                .title(req.title())
                .description(req.description())
                .priority(req.priority())
                .build());
        return detail(ticket);
    }

    public List<TicketSummary> list(AuthUser me, TicketStatus status, Long assigneeId) {
        return ticketRepository.search(me.companyId(), visibleRequesterId(me), status, assigneeId);
    }

    public TicketDetail get(AuthUser me, Long id) {
        return detail(findVisible(me, id));
    }

    @Transactional
    public TicketDetail changeStatus(AuthUser me, Long id, TicketStatus target) {
        Ticket ticket = findVisible(me, id);
        ticket.changeStatus(target);
        return detail(ticket);
    }

    @Transactional
    public TicketDetail assign(AuthUser me, Long id, Long assigneeId) {
        Ticket ticket = findInCompany(me, id);
        User assignee = userRepository.findByIdAndCompanyId(assigneeId, me.companyId())
                .orElseThrow(() -> new NotFoundException("담당자를 찾을 수 없습니다: " + assigneeId));
        ticket.assignTo(assignee);
        return detail(ticket);
    }

    @Transactional
    public TicketDetail addComment(AuthUser me, Long id, String message) {
        Ticket ticket = findVisible(me, id);
        User author = userRepository.findByIdAndCompanyId(me.userId(), me.companyId())
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
        commentRepository.save(TicketComment.builder()
                .ticket(ticket).author(author).message(message).build());
        return detail(ticket);
    }

    @Transactional
    public void delete(AuthUser me, Long id) {
        Ticket ticket = findInCompany(me, id);
        commentRepository.deleteByTicketId(ticket.getId());
        ticketRepository.delete(ticket);
    }

    public TicketStatsResponse stats(AuthUser me) {
        Map<TicketStatus, Long> counts = new EnumMap<>(TicketStatus.class);
        for (Object[] row : ticketRepository.countByStatus(me.companyId(), visibleRequesterId(me))) {
            counts.put((TicketStatus) row[0], (Long) row[1]);
        }
        return new TicketStatsResponse(
                counts.getOrDefault(TicketStatus.OPEN, 0L),
                counts.getOrDefault(TicketStatus.IN_PROGRESS, 0L),
                counts.getOrDefault(TicketStatus.RESOLVED, 0L),
                counts.getOrDefault(TicketStatus.CLOSED, 0L));
    }

    private Long visibleRequesterId(AuthUser me) {
        return me.isAdmin() ? null : me.userId();
    }

    private Ticket findInCompany(AuthUser me, Long id) {
        return ticketRepository.findByIdAndCompanyId(id, me.companyId())
                .orElseThrow(() -> new NotFoundException("티켓을 찾을 수 없습니다: " + id));
    }

    private Ticket findVisible(AuthUser me, Long id) {
        Ticket ticket = findInCompany(me, id);
        if (!me.isAdmin() && !ticket.getRequester().getId().equals(me.userId())) {
            throw new NotFoundException("티켓을 찾을 수 없습니다: " + id);
        }
        return ticket;
    }

    private TicketDetail detail(Ticket ticket) {
        return TicketDetail.of(ticket, commentRepository.findByTicketIdOrderByCreatedAtAsc(ticket.getId()));
    }
}
