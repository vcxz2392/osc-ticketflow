package com.ticketflow.common.security;

import com.ticketflow.user.domain.Role;

public record AuthUser(Long userId, Long companyId, String username, Role role) {

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }
}
