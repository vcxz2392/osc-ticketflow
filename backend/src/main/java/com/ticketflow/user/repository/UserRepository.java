package com.ticketflow.user.repository;

import com.ticketflow.user.domain.Role;
import com.ticketflow.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    List<User> findByCompanyIdAndRole(Long companyId, Role role);

    List<User> findByCompanyId(Long companyId);

    Optional<User> findByIdAndCompanyId(Long id, Long companyId);
}
