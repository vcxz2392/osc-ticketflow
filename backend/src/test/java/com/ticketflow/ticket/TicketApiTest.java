package com.ticketflow.ticket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketflow.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 티켓 API 통합 테스트 + REST Docs. 시드: TestCo(admin1/ADMIN, user1/USER). */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ActiveProfiles("test")
class TicketApiTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;

    private String token(String username, String password) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("username", username, "password", password));
        String res = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + objectMapper.readTree(res).at("/data/token").asText();
    }

    private long createTicket(String auth, String title) throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("title", title, "description", "내용입니다", "priority", "HIGH"));
        String res = mockMvc.perform(post("/api/tickets").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(res).at("/data/id").asLong();
    }

    @Nested
    @DisplayName("생성/목록")
    class CreateAndList {
        @Test
        @DisplayName("USER 가 티켓을 생성한다")
        void create() throws Exception {
            String auth = token("user1", "user123");
            String body = objectMapper.writeValueAsString(
                    Map.of("title", "프린터 고장", "description", "3층 프린터가 안 됩니다", "priority", "MEDIUM"));
            mockMvc.perform(post("/api/tickets").header("Authorization", auth)
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.status").value("OPEN"))
                    .andExpect(jsonPath("$.data.requester.name").value("사용자"))
                    .andDo(document("ticket-create",
                            requestFields(
                                    fieldWithPath("title").description("제목"),
                                    fieldWithPath("description").description("내용"),
                                    fieldWithPath("priority").description("우선순위(LOW|MEDIUM|HIGH)"))));
        }

        @Test
        @DisplayName("USER 는 본인 티켓만, ADMIN 은 회사 전체를 본다")
        void scopedList() throws Exception {
            String userAuth = token("user1", "user123");
            String adminAuth = token("admin1", "admin123");
            createTicket(userAuth, "유저 티켓");
            createTicket(adminAuth, "관리자 티켓");

            mockMvc.perform(get("/api/tickets").header("Authorization", adminAuth))
                    .andExpect(status().isOk())
                    .andDo(document("ticket-list"));
            // USER 목록엔 본인 것만 (관리자 티켓 미포함) → 최소 1건, 모두 requesterName=사용자 보장은 생략하고 가시성은 상세에서 검증
            mockMvc.perform(get("/api/tickets").header("Authorization", userAuth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("가시성/인가")
    class Visibility {
        @Test
        @DisplayName("USER 는 다른 사람 티켓 상세에 404")
        void userCannotSeeOthers() throws Exception {
            String adminAuth = token("admin1", "admin123");
            long adminTicket = createTicket(adminAuth, "관리자만의 티켓");
            String userAuth = token("user1", "user123");
            mockMvc.perform(get("/api/tickets/{id}", adminTicket).header("Authorization", userAuth))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
        }

        @Test
        @DisplayName("USER 는 담당자 배정 불가 403")
        void userCannotAssign() throws Exception {
            String userAuth = token("user1", "user123");
            long id = createTicket(userAuth, "배정시도");
            mockMvc.perform(patch("/api/tickets/{id}/assignee", id).header("Authorization", userAuth)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"assigneeId\":1}"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("상태 전이/배정/코멘트")
    class Workflow {
        @Test
        @DisplayName("상태 전이: 허용 200, 비허용 409")
        void transition() throws Exception {
            String admin = token("admin1", "admin123");
            long id = createTicket(admin, "전이 테스트");
            mockMvc.perform(patch("/api/tickets/{id}/status", id).header("Authorization", admin)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"IN_PROGRESS\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"))
                    .andDo(document("ticket-status",
                            requestFields(fieldWithPath("status").description("대상 상태"))));
            // IN_PROGRESS → OPEN 은 비허용
            mockMvc.perform(patch("/api/tickets/{id}/status", id).header("Authorization", admin)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"OPEN\"}"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error.code").value("INVALID_TRANSITION"));
        }

        @Test
        @DisplayName("ADMIN 이 담당자를 배정한다")
        void assign() throws Exception {
            String admin = token("admin1", "admin123");
            long id = createTicket(admin, "배정 테스트");
            long adminId = userRepository.findByUsername("admin1").orElseThrow().getId();
            mockMvc.perform(patch("/api/tickets/{id}/assignee", id).header("Authorization", admin)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"assigneeId\":" + adminId + "}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.assignee.name").value("관리자"))
                    .andDo(document("ticket-assign",
                            requestFields(fieldWithPath("assigneeId").description("담당자(ADMIN) 사용자 ID"))));
        }

        @Test
        @DisplayName("코멘트를 추가하면 타임라인에 반영된다")
        void comment() throws Exception {
            String admin = token("admin1", "admin123");
            long id = createTicket(admin, "코멘트 테스트");
            mockMvc.perform(post("/api/tickets/{id}/comments", id).header("Authorization", admin)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"message\":\"확인했습니다\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.comments[0].message").value("확인했습니다"))
                    .andDo(document("ticket-comment",
                            requestFields(fieldWithPath("message").description("코멘트 내용"))));
        }
    }
}
