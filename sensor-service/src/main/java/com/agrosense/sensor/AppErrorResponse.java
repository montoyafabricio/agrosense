package com.agrosense.sensor;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;

public record AppErrorResponse(ErrorDetail error) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ErrorDetail(
        String code,
        String message,
        Object details,
        OffsetDateTime timestamp,
        String path
    ) {}

    public static AppErrorResponse of(String code, String message, Object details, String path) {
        return new AppErrorResponse(
            new ErrorDetail(code, message, details, OffsetDateTime.now(), path)
        );
    }
}
