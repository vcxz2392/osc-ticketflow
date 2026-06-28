package com.ticketflow.ticket.repository;

import com.ticketflow.ticket.domain.TicketStatus;
import com.ticketflow.ticket.dto.TicketSummary;

import java.util.List;

public interface TicketRepositoryCustom {

    List<TicketSummary> search(Long companyId, Long requesterId, TicketStatus status, Long assigneeId);

    List<Object[]> countByStatus(Long companyId, Long requesterId);
}
