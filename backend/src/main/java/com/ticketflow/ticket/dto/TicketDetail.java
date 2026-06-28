package com.ticketflow.ticket.dto;

import com.ticketflow.ticket.domain.Priority;
import com.ticketflow.ticket.domain.Ticket;
import com.ticketflow.ticket.domain.TicketComment;
import com.ticketflow.ticket.domain.TicketStatus;

import java.time.Instant;
import java.util.List;

public record TicketDetail(
        Long id, String title, String description, TicketStatus status, Priority priority,
        UserRef requester, UserRef assignee, List<CommentResponse> comments,
        Instant createdAt, Instant updatedAt
) {
    public static TicketDetail of(Ticket t, List<TicketComment> comments) {
        return new TicketDetail(
                t.getId(), t.getTitle(), t.getDescription(), t.getStatus(), t.getPriority(),
                UserRef.from(t.getRequester()), UserRef.from(t.getAssignee()),
                comments.stream().map(CommentResponse::from).toList(),
                t.getCreatedAt(), t.getUpdatedAt());
    }
}
