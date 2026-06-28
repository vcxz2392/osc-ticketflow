package com.ticketflow.ticket.repository;

import com.ticketflow.ticket.domain.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long>, TicketRepositoryCustom {

    Optional<Ticket> findByIdAndCompanyId(Long id, Long companyId);
}
