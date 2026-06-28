package com.ticketflow.user.service;

import com.ticketflow.common.exception.DuplicateException;
import com.ticketflow.common.security.AuthUser;
import com.ticketflow.company.domain.Company;
import com.ticketflow.company.repository.CompanyRepository;
import com.ticketflow.user.domain.Role;
import com.ticketflow.user.domain.User;
import com.ticketflow.user.dto.CreateUserRequest;
import com.ticketflow.user.repository.UserRepository;
import com.ticketflow.user.dto.UserResponse;
import com.ticketflow.user.dto.UserSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse create(AuthUser me, CreateUserRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new DuplicateException("이미 사용 중인 아이디입니다.");
        }
        Company company = companyRepository.findById(me.companyId())
                .orElseThrow(() -> new com.ticketflow.common.exception.NotFoundException("회사를 찾을 수 없습니다."));
        User user = userRepository.save(User.builder()
                .company(company)
                .username(req.username())
                .passwordHash(passwordEncoder.encode(req.password()))
                .name(req.name())
                .role(req.role())
                .build());
        return UserResponse.from(user);
    }

    public List<UserSummary> list(AuthUser me, Role role) {
        List<User> users = (role != null)
                ? userRepository.findByCompanyIdAndRole(me.companyId(), role)
                : userRepository.findByCompanyId(me.companyId());
        return users.stream().map(UserSummary::from).toList();
    }
}
