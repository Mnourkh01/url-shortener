package com.shortener.common.error.exceptions

import com.shortener.common.error.ErrorCode

class BadRequestException(
    override val message: String,
    val errorCode: ErrorCode = ErrorCode.BAD_REQUEST
) : RuntimeException(message)

class NotFoundException(
    override val message: String,
    val errorCode: ErrorCode = ErrorCode.RESOURCE_NOT_FOUND
) : RuntimeException(message)

class ConflictException(
    override val message: String,
    val errorCode: ErrorCode = ErrorCode.RESOURCE_ALREADY_EXISTS
) : RuntimeException(message)
