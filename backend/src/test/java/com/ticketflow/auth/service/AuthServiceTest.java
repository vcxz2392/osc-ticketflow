package com.ticketflow.auth.service;

import com.ticketflow.auth.dto.AuthResponse;
import com.ticketflow.auth.dto.LoginRequest;
import com.ticketflow.auth.dto.SignupRequest;
import com.ticketflow.common.exception.DuplicateException;
import com.ticketflow.common.security.JwtTokenProvider;
import com.ticketflow.company.domain.Company;
import com.ticketflow.company.repository.CompanyRepository;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock CompanyRepository companyRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtTokenProvider tokenProvider;
    @InjectMocks AuthService authService;

    private User user(String username, Role role) {
        return User.builder().company(Company.builder().name("Acme").build())
                .username(username).passwordHash("hash").name("이름").role(role).build();
    }

    @Nested
    @DisplayName("회사 가입")
    class Signup {
        @Test
        @DisplayName("새 회사와 ADMIN 을 만들고 토큰을 발급한다")
        void success() {
            given(userRepository.existsByUsername("owner")).willReturn(false);
            given(companyRepository.save(any())).willAnswer(i -> i.getArgument(0));
            given(userRepository.save(any())).willAnswer(i -> i.getArgument(0));
            given(passwordEncoder.encode("pw12345678")).willReturn("hash");
            given(tokenProvider.createToken(any())).willReturn("tok");

            AuthResponse res = authService.signup(new SignupRequest("새회사", "owner", "pw12345678", "오너"));

            assertThat(res.token()).isEqualTo("tok");

            var captor = org.mockito.ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            User saved = captor.getValue();
            assertThat(saved.getUsername()).isEqualTo("owner");
            assertThat(saved.getRole()).isEqualTo(Role.ADMIN);
            assertThat(saved.getPasswordHash()).isEqualTo("hash");
            assertThat(saved.getCompany().getName()).isEqualTo("새회사");
        }

        @Test
        @DisplayName("아이디 중복이면 DuplicateException")
        void duplicate() {
            given(userRepository.existsByUsername("dup")).willReturn(true);
            assertThatThrownBy(() -> authService.signup(new SignupRequest("c", "dup", "pw12345678", "n")))
                    .isInstanceOf(DuplicateException.class);
        }
    }

    @Nested
    @DisplayName("로그인")
    class Login {
        @Test
        @DisplayName("성공 시 토큰과 사용자 정보 반환")
        void success() {
            given(userRepository.findByUsername("admin1")).willReturn(Optional.of(user("admin1", Role.ADMIN)));
            given(passwordEncoder.matches("pw", "hash")).willReturn(true);
            given(tokenProvider.createToken(any())).willReturn("tok");

            AuthResponse res = authService.login(new LoginRequest("admin1", "pw"));

            assertThat(res.token()).isEqualTo("tok");
            assertThat(res.user().role()).isEqualTo(Role.ADMIN);
        }

        @Test
        @DisplayName("없는 아이디면 BadCredentials")
        void noUser() {
            given(userRepository.findByUsername("none")).willReturn(Optional.empty());
            assertThatThrownBy(() -> authService.login(new LoginRequest("none", "pw")))
                    .isInstanceOf(BadCredentialsException.class);
        }

        @Test
        @DisplayName("비밀번호 불일치면 BadCredentials")
        void wrongPassword() {
            given(userRepository.findByUsername("admin1")).willReturn(Optional.of(user("admin1", Role.ADMIN)));
            given(passwordEncoder.matches("bad", "hash")).willReturn(false);
            assertThatThrownBy(() -> authService.login(new LoginRequest("admin1", "bad")))
                    .isInstanceOf(BadCredentialsException.class);
        }
    }
}
