package com.ticketflow.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddCommentRequest(@NotBlank @Size(max = 1000) String message) {
}
