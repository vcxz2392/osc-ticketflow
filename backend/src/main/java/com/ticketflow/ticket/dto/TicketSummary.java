package com.ticketflow.ticket.dto;

import com.ticketflow.ticket.domain.Priority;
import com.ticketflow.ticket.domain.TicketStatus;

import java.time.Instant;

public record TicketSummary(
        Long id, String title, TicketStatus status, Priority priority,
        String requesterName, String assigneeName, Instant createdAt
) {
}
