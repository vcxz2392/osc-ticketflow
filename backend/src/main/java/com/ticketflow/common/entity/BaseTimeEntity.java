package com.ticketflow.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

/** 생성·수정 시각을 갖는 엔티티의 공통 부모. updated_at 을 자동 갱신한다. */
@Getter
@MappedSuperclass
public abstract class BaseTimeEntity extends BaseCreatedEntity {

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
