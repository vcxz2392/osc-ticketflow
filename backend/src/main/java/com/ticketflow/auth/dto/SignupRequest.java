package com.ticketflow.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record SignupRequest(
        @NotBlank String companyName,
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String name
) {
}
