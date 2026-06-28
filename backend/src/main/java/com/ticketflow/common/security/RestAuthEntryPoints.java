package com.ticketflow.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketflow.common.exception.ErrorCode;
import com.ticketflow.common.response.ApiError;
import com.ticketflow.common.response.ApiResult;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RestAuthEntryPoints {

    private final ObjectMapper objectMapper;

    public AuthenticationEntryPoint unauthorized() {
        return (request, response, ex) -> write(response, ErrorCode.UNAUTHORIZED);
    }

    public AccessDeniedHandler forbidden() {
        return (request, response, ex) -> write(response, ErrorCode.FORBIDDEN);
    }

    private void write(HttpServletResponse response, ErrorCode code) throws IOException {
        response.setStatus(code.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        var body = ApiResult.error(new ApiError(code.name(), code.getDefaultMessage()));
        objectMapper.writeValue(response.getWriter(), body);
    }
}
