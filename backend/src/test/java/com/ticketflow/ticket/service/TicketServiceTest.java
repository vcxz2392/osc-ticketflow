package com.ticketflow.ticket.service;

import com.ticketflow.common.exception.NotFoundException;
import com.ticketflow.common.security.AuthUser;
import com.ticketflow.ticket.domain.Ticket;
import com.ticketflow.ticket.domain.TicketStatus;
import com.ticketflow.ticket.dto.TicketStatsResponse;
import com.ticketflow.ticket.repository.TicketCommentRepository;
import com.ticketflow.ticket.repository.TicketRepository;
import com.ticketflow.user.domain.Role;
import com.ticketflow.user.domain.User;
import com.ticketflow.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock TicketRepository ticketRepository;
    @Mock TicketCommentRepository commentRepository;
    @Mock UserRepository userRepository;
    @InjectMocks TicketService ticketService;

    private final AuthUser admin = new AuthUser(1L, 100L, "admin1", Role.ADMIN);
    private final AuthUser user = new AuthUser(2L, 100L, "user1", Role.USER);

    @Nested
    @DisplayName("가시성/테넌트")
    class Visibility {
        @Test
        @DisplayName("다른 회사 티켓이면 404 (회사 스코프 조회 결과 없음)")
        void notInCompany() {
            given(ticketRepository.findByIdAndCompanyId(5L, 100L)).willReturn(Optional.empty());
            assertThatThrownBy(() -> ticketService.get(user, 5L)).isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("USER 가 본인 요청분이 아니면 404 (존재 비노출)")
        void userCannotSeeOthers() {
            Ticket t = org.mockito.Mockito.mock(Ticket.class);
            User otherRequester = org.mockito.Mockito.mock(User.class);
            given(otherRequester.getId()).willReturn(999L);
            given(t.getRequester()).willReturn(otherRequester);
            given(ticketRepository.findByIdAndCompanyId(5L, 100L)).willReturn(Optional.of(t));

            assertThatThrownBy(() -> ticketService.get(user, 5L)).isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("목록 스코프")
    class ListScope {
        @Test
        @DisplayName("USER 는 본인 요청분으로 스코프(requesterId=me)")
        void userScoped() {
            ticketService.list(user, null, null);
            verify(ticketRepository).search(100L, 2L, null, null);
        }

        @Test
        @DisplayName("ADMIN 은 회사 전체(requesterId=null)")
        void adminAll() {
            ticketService.list(admin, TicketStatus.OPEN, null);
            verify(ticketRepository).search(100L, null, TicketStatus.OPEN, null);
        }
    }

    @Nested
    @DisplayName("전이/배정 위임")
    class Mutations {
        @Test
        @DisplayName("changeStatus 는 도메인 전이 메서드에 위임한다")
        void changeStatusDelegates() {
            Ticket t = org.mockito.Mockito.mock(Ticket.class);
            given(ticketRepository.findByIdAndCompanyId(5L, 100L)).willReturn(Optional.of(t));
            lenient().when(commentRepository.findByTicketIdOrderByCreatedAtAsc(any())).thenReturn(List.of());

            ticketService.changeStatus(admin, 5L, TicketStatus.IN_PROGRESS);

            verify(t).changeStatus(TicketStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("assign 은 회사 스코프 담당자를 로드해 도메인 배정에 위임한다")
        void assignDelegates() {
            Ticket t = org.mockito.Mockito.mock(Ticket.class);
            User assignee = org.mockito.Mockito.mock(User.class);
            given(ticketRepository.findByIdAndCompanyId(5L, 100L)).willReturn(Optional.of(t));
            given(userRepository.findByIdAndCompanyId(3L, 100L)).willReturn(Optional.of(assignee));
            lenient().when(commentRepository.findByTicketIdOrderByCreatedAtAsc(any())).thenReturn(List.of());

            ticketService.assign(admin, 5L, 3L);

            verify(t).assignTo(assignee);
        }

        @Test
        @DisplayName("배정 대상이 회사에 없으면 404")
        void assigneeNotInCompany() {
            Ticket t = org.mockito.Mockito.mock(Ticket.class);
            given(ticketRepository.findByIdAndCompanyId(5L, 100L)).willReturn(Optional.of(t));
            given(userRepository.findByIdAndCompanyId(999L, 100L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> ticketService.assign(admin, 5L, 999L))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("집계")
    class Stats {
        @Test
        @DisplayName("상태별 카운트를 응답으로 매핑한다")
        void mapping() {
            given(ticketRepository.countByStatus(100L, null)).willReturn(List.<Object[]>of(
                    new Object[]{TicketStatus.OPEN, 2L},
                    new Object[]{TicketStatus.RESOLVED, 1L}));

            TicketStatsResponse res = ticketService.stats(admin);

            assertThat(res.open()).isEqualTo(2);
            assertThat(res.resolved()).isEqualTo(1);
            assertThat(res.inProgress()).isZero();
            assertThat(res.closed()).isZero();
        }

        @Test
        @DisplayName("USER 집계는 본인 요청분으로 스코프된다")
        void userScoped() {
            given(ticketRepository.countByStatus(100L, 2L)).willReturn(List.of());
            ticketService.stats(user);
            verify(ticketRepository).countByStatus(100L, 2L);
        }
    }
}
