package com.ticketflow.ticket.dto;

import com.ticketflow.ticket.domain.TicketComment;

import java.time.Instant;

public record CommentResponse(Long id, String authorName, String message, Instant createdAt) {
    public static CommentResponse from(TicketComment c) {
        return new CommentResponse(c.getId(), c.getAuthor().getName(), c.getMessage(), c.getCreatedAt());
    }
}
