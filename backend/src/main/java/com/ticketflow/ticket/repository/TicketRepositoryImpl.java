package com.ticketflow.ticket.repository;

import com.ticketflow.ticket.domain.Priority;
import com.ticketflow.ticket.domain.TicketStatus;
import com.ticketflow.ticket.dto.TicketSummary;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@RequiredArgsConstructor
public class TicketRepositoryImpl implements TicketRepositoryCustom {

    private final EntityManager em;

    @Override
    @SuppressWarnings("unchecked")
    public List<TicketSummary> search(Long companyId, Long requesterId, TicketStatus status, Long assigneeId) {
        StringBuilder sql = new StringBuilder("""
                select t.id, t.title, t.status, t.priority, r.name, a.name, t.created_at
                from tickets t
                join users r on r.id = t.requester_id
                left join users a on a.id = t.assignee_id
                where t.company_id = :companyId
                """);

        if (requesterId != null) sql.append(" and t.requester_id = :requesterId");
        if (status != null)      sql.append(" and t.status = :status");
        if (assigneeId != null)  sql.append(" and t.assignee_id = :assigneeId");
        sql.append(" order by t.created_at desc");

        var query = em.createNativeQuery(sql.toString()).setParameter("companyId", companyId);
        if (requesterId != null) query.setParameter("requesterId", requesterId);
        if (status != null)      query.setParameter("status", status.name());
        if (assigneeId != null)  query.setParameter("assigneeId", assigneeId);
        List<Object[]> rows = query.getResultList();

        return rows.stream().map(r -> new TicketSummary(
                ((Number) r[0]).longValue(),
                (String) r[1],
                TicketStatus.valueOf(String.valueOf(r[2])),
                Priority.valueOf(String.valueOf(r[3])),
                (String) r[4],
                (String) r[5],
                toInstant(r[6]))).toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object[]> countByStatus(Long companyId, Long requesterId) {
        StringBuilder sql = new StringBuilder("""
                select t.status, count(*)
                from tickets t
                where t.company_id = :companyId
                """);
        if (requesterId != null) sql.append(" and t.requester_id = :requesterId");
        sql.append(" group by t.status");

        var query = em.createNativeQuery(sql.toString()).setParameter("companyId", companyId);
        if (requesterId != null) query.setParameter("requesterId", requesterId);
        List<Object[]> rows = query.getResultList();

        return rows.stream()
                .map(r -> new Object[]{TicketStatus.valueOf(String.valueOf(r[0])), ((Number) r[1]).longValue()})
                .toList();
    }

    private Instant toInstant(Object value) {
        if (value == null) return null;
        if (value instanceof Instant i) return i;
        if (value instanceof OffsetDateTime odt) return odt.toInstant();
        if (value instanceof Timestamp ts) return ts.toInstant();
        if (value instanceof LocalDateTime ldt) return ldt.toInstant(ZoneOffset.UTC);
        throw new IllegalStateException("지원하지 않는 시간 타입: " + value.getClass());
    }
}
