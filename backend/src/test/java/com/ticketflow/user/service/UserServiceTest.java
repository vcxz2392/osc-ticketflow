package com.ticketflow.user.service;

import com.ticketflow.common.exception.DuplicateException;
import com.ticketflow.common.security.AuthUser;
import com.ticketflow.company.domain.Company;
import com.ticketflow.company.repository.CompanyRepository;
import com.ticketflow.user.domain.Role;
import com.ticketflow.user.repository.UserRepository;
import com.ticketflow.user.dto.CreateUserRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock CompanyRepository companyRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks UserService userService;

    private final AuthUser admin = new AuthUser(1L, 10L, "admin1", Role.ADMIN);

    @Nested
    @DisplayName("멤버 추가")
    class Create {
        @Test
        @DisplayName("아이디 중복이면 DuplicateException")
        void duplicate() {
            given(userRepository.existsByUsername("dup")).willReturn(true);
            assertThatThrownBy(() -> userService.create(admin,
                    new CreateUserRequest("dup", "pw12345678", "n", Role.USER)))
                    .isInstanceOf(DuplicateException.class);
        }

        @Test
        @DisplayName("내 회사(me.companyId)로 사용자를 저장한다")
        void savesInMyCompany() {
            given(userRepository.existsByUsername("new")).willReturn(false);
            given(companyRepository.findById(10L)).willReturn(Optional.of(Company.builder().name("C").build()));
            given(passwordEncoder.encode(any())).willReturn("hash");
            given(userRepository.save(any())).willAnswer(i -> i.getArgument(0));

            userService.create(admin, new CreateUserRequest("new", "pw12345678", "이름", Role.USER));

            verify(companyRepository).findById(10L);
            var captor = org.mockito.ArgumentCaptor.forClass(com.ticketflow.user.domain.User.class);
            verify(userRepository).save(captor.capture());
            var saved = captor.getValue();
            assertThat(saved.getUsername()).isEqualTo("new");
            assertThat(saved.getRole()).isEqualTo(Role.USER);
            assertThat(saved.getPasswordHash()).isEqualTo("hash");
            assertThat(saved.getCompany().getName()).isEqualTo("C");
        }
    }

    @Nested
    @DisplayName("사용자 목록")
    class ListUsers {
        @Test
        @DisplayName("role 을 주면 회사+역할로 조회")
        void byRole() {
            given(userRepository.findByCompanyIdAndRole(10L, Role.ADMIN)).willReturn(List.of());
            userService.list(admin, Role.ADMIN);
            verify(userRepository).findByCompanyIdAndRole(10L, Role.ADMIN);
        }

        @Test
        @DisplayName("role 이 없으면 회사 전체로 조회")
        void allInCompany() {
            given(userRepository.findByCompanyId(10L)).willReturn(List.of());
            userService.list(admin, null);
            verify(userRepository).findByCompanyId(10L);
        }
    }
}
