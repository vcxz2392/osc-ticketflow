package com.ticketflow.user.dto;

import com.ticketflow.user.domain.Role;
import com.ticketflow.user.domain.User;

public record UserSummary(Long id, String username, String name, Role role) {
    public static UserSummary from(User u) {
        return new UserSummary(u.getId(), u.getUsername(), u.getName(), u.getRole());
    }
}
