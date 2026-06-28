package com.ticketflow.ticket.repository;

import com.ticketflow.ticket.domain.TicketComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {

    @Query("select c from TicketComment c join fetch c.author where c.ticket.id = :ticketId order by c.createdAt asc")
    List<TicketComment> findByTicketIdOrderByCreatedAtAsc(@Param("ticketId") Long ticketId);

    void deleteByTicketId(Long ticketId);
}
