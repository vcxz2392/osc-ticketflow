package com.ticketflow.user.dto;

import com.ticketflow.user.domain.Role;
import com.ticketflow.user.domain.User;

public record UserResponse(Long id, String username, String name, Role role, Long companyId, String companyName) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getName(), u.getRole(),
                u.getCompany().getId(), u.getCompany().getName());
    }
}
