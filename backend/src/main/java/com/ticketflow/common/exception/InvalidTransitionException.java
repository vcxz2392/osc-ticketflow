package com.ticketflow.common.exception;

public class InvalidTransitionException extends BusinessException {
    public InvalidTransitionException(String message) {
        super(ErrorCode.INVALID_TRANSITION, message);
    }
}
