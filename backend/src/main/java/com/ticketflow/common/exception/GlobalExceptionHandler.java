package com.ticketflow.common.exception;

import com.ticketflow.common.response.ApiError;
import com.ticketflow.common.response.ApiResult;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResult<Void>> handleBusiness(BusinessException e) {
        return build(e.getErrorCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Void>> handleValidation(MethodArgumentNotValidException e) {
        var br = e.getBindingResult();
        String message = Stream.concat(
                        br.getFieldErrors().stream().map(fe -> fe.getField() + ": " + fe.getDefaultMessage()),
                        br.getGlobalErrors().stream().map(ge -> ge.getDefaultMessage()))
                .collect(Collectors.joining(", "));
        return build(ErrorCode.INVALID_INPUT,
                message.isBlank() ? ErrorCode.INVALID_INPUT.getDefaultMessage() : message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResult<Void>> handleConstraint(ConstraintViolationException e) {
        return build(ErrorCode.INVALID_INPUT, e.getMessage());
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ApiResult<Void>> handleMalformedRequest(Exception e) {
        log.debug("잘못된 요청: {}", e.getMessage());
        return build(ErrorCode.INVALID_INPUT, ErrorCode.INVALID_INPUT.getDefaultMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResult<Void>> handleBadCredentials(BadCredentialsException e) {
        return build(ErrorCode.INVALID_CREDENTIALS, e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResult<Void>> handleDataIntegrity(DataIntegrityViolationException e) {
        log.debug("무결성 제약 위반: {}", e.getMessage());
        return build(ErrorCode.DUPLICATE, ErrorCode.DUPLICATE.getDefaultMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResult<Void>> handleAccessDenied(AccessDeniedException e) {
        return build(ErrorCode.FORBIDDEN, ErrorCode.FORBIDDEN.getDefaultMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Void>> handleUnexpected(Exception e) {
        log.error("처리되지 않은 예외", e);
        return build(ErrorCode.INTERNAL_ERROR, ErrorCode.INTERNAL_ERROR.getDefaultMessage());
    }

    private ResponseEntity<ApiResult<Void>> build(ErrorCode code, String message) {
        return ResponseEntity.status(code.getStatus())
                .body(ApiResult.error(new ApiError(code.name(), message)));
    }
}
