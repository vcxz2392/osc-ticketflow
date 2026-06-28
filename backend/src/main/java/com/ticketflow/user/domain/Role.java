package com.ticketflow.user.domain;

/** 사용자 권한. ADMIN(관리자)은 배정·전체조회·삭제, USER(요청자)는 티켓 생성/자기 티켓. */
public enum Role {
    ADMIN, USER
}
