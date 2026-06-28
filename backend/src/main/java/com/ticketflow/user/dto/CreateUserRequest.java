package com.ticketflow.user.dto;

import com.ticketflow.user.domain.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank @Size(min = 4, max = 50) String username,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(max = 50) String name,
        @NotNull Role role
) {
}
