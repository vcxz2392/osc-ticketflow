package com.ticketflow.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResult<T>(boolean success, T data, ApiError error) {

    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(true, data, null);
    }

    public static ApiResult<Void> error(ApiError error) {
        return new ApiResult<>(false, null, error);
    }
}
