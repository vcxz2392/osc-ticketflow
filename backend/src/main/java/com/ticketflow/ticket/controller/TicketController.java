package com.ticketflow.ticket.controller;

import com.ticketflow.common.response.ApiResult;
import com.ticketflow.common.security.AuthUser;
import com.ticketflow.common.security.LoginUser;
import com.ticketflow.ticket.domain.TicketStatus;
import com.ticketflow.ticket.dto.*;
import com.ticketflow.ticket.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResult<TicketDetail> create(@LoginUser AuthUser me,
                                          @Valid @RequestBody CreateTicketRequest request) {
        return ApiResult.success(ticketService.create(me, request));
    }

    @GetMapping
    public ApiResult<List<TicketSummary>> list(@LoginUser AuthUser me,
                                               @RequestParam(required = false) TicketStatus status,
                                               @RequestParam(required = false) Long assigneeId) {
        return ApiResult.success(ticketService.list(me, status, assigneeId));
    }

    @GetMapping("/stats")
    public ApiResult<TicketStatsResponse> stats(@LoginUser AuthUser me) {
        return ApiResult.success(ticketService.stats(me));
    }

    @GetMapping("/{id}")
    public ApiResult<TicketDetail> get(@LoginUser AuthUser me, @PathVariable Long id) {
        return ApiResult.success(ticketService.get(me, id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<TicketDetail> changeStatus(@LoginUser AuthUser me, @PathVariable Long id,
                                                @Valid @RequestBody UpdateStatusRequest request) {
        return ApiResult.success(ticketService.changeStatus(me, id, request.status()));
    }

    @PatchMapping("/{id}/assignee")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<TicketDetail> assign(@LoginUser AuthUser me, @PathVariable Long id,
                                          @Valid @RequestBody AssignRequest request) {
        return ApiResult.success(ticketService.assign(me, id, request.assigneeId()));
    }

    @PostMapping("/{id}/comments")
    public ApiResult<TicketDetail> addComment(@LoginUser AuthUser me, @PathVariable Long id,
                                              @Valid @RequestBody AddCommentRequest request) {
        return ApiResult.success(ticketService.addComment(me, id, request.message()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@LoginUser AuthUser me, @PathVariable Long id) {
        ticketService.delete(me, id);
    }
}
