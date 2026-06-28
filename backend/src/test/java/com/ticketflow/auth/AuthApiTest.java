package com.ticketflow.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 인증 API 통합 테스트 + REST Docs. */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ActiveProfiles("test")
class AuthApiTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    private String json(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    @Nested
    @DisplayName("회사 가입(signup)")
    class Signup {

        @Test
        @DisplayName("새 회사와 ADMIN 사용자를 생성하고 토큰을 발급한다")
        void success() throws Exception {
            var body = json(Map.of("companyName", "새회사", "username", "owner1",
                    "password", "ownerpass1", "name", "오너"));
            mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.token").isNotEmpty())
                    .andExpect(jsonPath("$.data.user.role").value("ADMIN"))
                    .andExpect(jsonPath("$.data.user.companyName").value("새회사"))
                    .andDo(document("auth-signup",
                            requestFields(
                                    fieldWithPath("companyName").description("회사명"),
                                    fieldWithPath("username").description("아이디(전역 고유)"),
                                    fieldWithPath("password").description("비밀번호(8자 이상)"),
                                    fieldWithPath("name").description("사용자 이름")),
                            responseFields(
                                    fieldWithPath("success").description("성공 여부"),
                                    fieldWithPath("data.token").description("JWT 액세스 토큰"),
                                    fieldWithPath("data.user.id").description("사용자 ID"),
                                    fieldWithPath("data.user.username").description("아이디"),
                                    fieldWithPath("data.user.name").description("이름"),
                                    fieldWithPath("data.user.role").description("권한(ADMIN|USER)"),
                                    fieldWithPath("data.user.companyId").description("회사 ID"),
                                    fieldWithPath("data.user.companyName").description("회사명"))));
        }

        @Test
        @DisplayName("이미 존재하는 아이디면 409 DUPLICATE")
        void duplicate() throws Exception {
            var body = json(Map.of("companyName", "회사", "username", "admin1",
                    "password", "ownerpass1", "name", "중복"));
            mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("DUPLICATE"));
        }

        @Test
        @DisplayName("필수값 누락이면 400 INVALID_INPUT")
        void validation() throws Exception {
            var body = json(Map.of("companyName", "", "username", "x", "password", "short", "name", ""));
            mockMvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("INVALID_INPUT"));
        }
    }

    @Nested
    @DisplayName("로그인(login)")
    class Login {

        @Test
        @DisplayName("시드 사용자로 로그인하면 토큰과 사용자 정보를 반환한다")
        void success() throws Exception {
            var body = json(Map.of("username", "admin1", "password", "admin123"));
            mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.token").isNotEmpty())
                    .andExpect(jsonPath("$.data.user.role").value("ADMIN"))
                    .andDo(document("auth-login",
                            requestFields(
                                    fieldWithPath("username").description("아이디"),
                                    fieldWithPath("password").description("비밀번호")),
                            responseFields(
                                    fieldWithPath("success").description("성공 여부"),
                                    fieldWithPath("data.token").description("JWT 액세스 토큰"),
                                    fieldWithPath("data.user.id").description("사용자 ID"),
                                    fieldWithPath("data.user.username").description("아이디"),
                                    fieldWithPath("data.user.name").description("이름"),
                                    fieldWithPath("data.user.role").description("권한"),
                                    fieldWithPath("data.user.companyId").description("회사 ID"),
                                    fieldWithPath("data.user.companyName").description("회사명"))));
        }

        @Test
        @DisplayName("비밀번호가 틀리면 401 INVALID_CREDENTIALS")
        void wrongPassword() throws Exception {
            var body = json(Map.of("username", "admin1", "password", "wrongpass"));
            mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
        }
    }
}
