package com.shortener.common.error

import com.shortener.common.error.exceptions.BadRequestException
import com.shortener.common.error.exceptions.ConflictException
import com.shortener.common.error.exceptions.NotFoundException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.time.OffsetDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val details = ex.bindingResult.fieldErrors.map {
            FieldErrorResponse(field = it.field, message = it.defaultMessage ?: "Invalid value")
        }
        return build(ErrorCode.VALIDATION_ERROR, "Validation failed", request, details)
    }

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequest(ex: BadRequestException, request: HttpServletRequest) =
        build(ex.errorCode, ex.message, request)

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(ex: NotFoundException, request: HttpServletRequest) =
        build(ex.errorCode, ex.message, request)

    @ExceptionHandler(ConflictException::class)
    fun handleConflict(ex: ConflictException, request: HttpServletRequest) =
        build(ex.errorCode, ex.message, request)

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrity(ex: DataIntegrityViolationException, request: HttpServletRequest) =
        build(ErrorCode.RESOURCE_ALREADY_EXISTS, "Resource already exists", request)

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleNotReadable(ex: HttpMessageNotReadableException, request: HttpServletRequest) =
        build(ErrorCode.BAD_REQUEST, "Request body is missing or invalid", request)

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(ex: MethodArgumentTypeMismatchException, request: HttpServletRequest) =
        build(ErrorCode.BAD_REQUEST, "Invalid parameter: ${ex.name}", request)

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotAllowed(ex: HttpRequestMethodNotSupportedException, request: HttpServletRequest) =
        build(ErrorCode.METHOD_NOT_ALLOWED, ex.message ?: "Method not allowed", request)

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception, request: HttpServletRequest) =
        build(ErrorCode.INTERNAL_ERROR, "Unexpected error occurred", request)

    private fun build(
        errorCode: ErrorCode,
        message: String,
        request: HttpServletRequest,
        details: List<FieldErrorResponse>? = null
    ): ResponseEntity<ErrorResponse> {
        val httpStatus: HttpStatus = errorCode.httpStatus
        val body = ErrorResponse(
            timestamp = OffsetDateTime.now(),
            status = httpStatus.value(),
            error = httpStatus.reasonPhrase,
            code = errorCode.name,
            message = message,
            path = request.requestURI,
            details = details
        )
        return ResponseEntity.status(httpStatus).body(body)
    }
}
