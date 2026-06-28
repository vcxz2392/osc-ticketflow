package com.ticketflow.ticket.domain;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * 티켓 상태와 허용 전이 규칙.
 * OPEN → IN_PROGRESS|CLOSED, IN_PROGRESS → RESOLVED|CLOSED, RESOLVED → CLOSED|IN_PROGRESS(재오픈), CLOSED → (종료)
 */
public enum TicketStatus {
    OPEN, IN_PROGRESS, RESOLVED, CLOSED;

    private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED = Map.of(
            OPEN, EnumSet.of(IN_PROGRESS, CLOSED),
            IN_PROGRESS, EnumSet.of(RESOLVED, CLOSED),
            RESOLVED, EnumSet.of(CLOSED, IN_PROGRESS),
            CLOSED, EnumSet.noneOf(TicketStatus.class)
    );

    public boolean canTransitionTo(TicketStatus target) {
        return target != null && ALLOWED.get(this).contains(target);
    }
}
