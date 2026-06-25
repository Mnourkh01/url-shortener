package com.shortener.common.error

import org.springframework.http.HttpStatus

enum class ErrorCode(val httpStatus: HttpStatus) {
    BAD_REQUEST(HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR(HttpStatus.UNPROCESSABLE_ENTITY),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND),
    RESOURCE_ALREADY_EXISTS(HttpStatus.CONFLICT),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);
}
