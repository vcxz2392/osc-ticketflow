package com.ticketflow.ticket.dto;

import com.ticketflow.user.domain.User;

public record UserRef(Long id, String name) {
    public static UserRef from(User u) {
        return u == null ? null : new UserRef(u.getId(), u.getName());
    }
}
