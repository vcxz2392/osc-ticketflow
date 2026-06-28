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
import com.ticketflow.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public AuthResponse signup(SignupRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new DuplicateException("이미 사용 중인 아이디입니다.");
        }
        Company company = companyRepository.save(Company.builder().name(req.companyName()).build());
        User user = userRepository.save(User.builder()
                .company(company)
                .username(req.username())
                .passwordHash(passwordEncoder.encode(req.password()))
                .name(req.name())
                .role(Role.ADMIN)
                .build());
        return new AuthResponse(tokenProvider.createToken(user), UserResponse.from(user));
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new BadCredentialsException("아이디 또는 비밀번호가 올바르지 않습니다."));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        return new AuthResponse(tokenProvider.createToken(user), UserResponse.from(user));
    }
}
