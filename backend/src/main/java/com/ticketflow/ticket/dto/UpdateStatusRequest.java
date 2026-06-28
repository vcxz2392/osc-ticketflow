package com.ticketflow.ticket.dto;

import com.ticketflow.ticket.domain.TicketStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(@NotNull TicketStatus status) {
}
