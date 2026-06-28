package com.ticketflow.user.dto;

import com.ticketflow.user.domain.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String name,
        @NotNull Role role
) {
}
