package com.ticketflow.auth.dto;

import com.ticketflow.user.dto.UserResponse;

public record AuthResponse(String token, UserResponse user) {
}
