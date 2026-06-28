package com.ticketflow.ticket;

import com.ticketflow.ticket.domain.TicketStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.ticketflow.ticket.domain.TicketStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

/** 상태 전이 규칙 단위 테스트. */
class TicketStatusTest {

    @Nested
    @DisplayName("OPEN 에서")
    class FromOpen {
        @Test void 진행중_종료_가능() {
            assertThat(OPEN.canTransitionTo(IN_PROGRESS)).isTrue();
            assertThat(OPEN.canTransitionTo(CLOSED)).isTrue();
        }
        @Test void 해결로_직접_불가() {
            assertThat(OPEN.canTransitionTo(RESOLVED)).isFalse();
        }
    }

    @Nested
    @DisplayName("RESOLVED 에서")
    class FromResolved {
        @Test void 종료_재오픈_가능() {
            assertThat(RESOLVED.canTransitionTo(CLOSED)).isTrue();
            assertThat(RESOLVED.canTransitionTo(IN_PROGRESS)).isTrue();
        }
    }

    @Nested
    @DisplayName("CLOSED 에서")
    class FromClosed {
        @Test void 어떤_전이도_불가() {
            for (TicketStatus s : TicketStatus.values()) {
                assertThat(CLOSED.canTransitionTo(s)).isFalse();
            }
        }
    }

    @Test
    @DisplayName("null 대상은 false (NPE 아님)")
    void nullTarget() {
        assertThat(OPEN.canTransitionTo(null)).isFalse();
    }
}
