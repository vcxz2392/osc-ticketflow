package com.ticketflow.ticket.dto;

public record TicketStatsResponse(long open, long inProgress, long resolved, long closed) {
}
