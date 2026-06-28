package com.ticketflow.user.controller;

import com.ticketflow.common.response.ApiResult;
import com.ticketflow.common.security.AuthUser;
import com.ticketflow.common.security.LoginUser;
import com.ticketflow.user.domain.Role;
import com.ticketflow.user.dto.CreateUserRequest;
import com.ticketflow.user.service.UserService;
import com.ticketflow.user.dto.UserResponse;
import com.ticketflow.user.dto.UserSummary;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResult<UserResponse> create(@LoginUser AuthUser me,
                                          @Valid @RequestBody CreateUserRequest request) {
        return ApiResult.success(userService.create(me, request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<List<UserSummary>> list(@LoginUser AuthUser me,
                                             @RequestParam(required = false) Role role) {
        return ApiResult.success(userService.list(me, role));
    }
}
