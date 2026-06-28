package com.ticketflow.ticket.dto;

import com.ticketflow.ticket.domain.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 2000) String description,
        @NotNull Priority priority
) {
}
