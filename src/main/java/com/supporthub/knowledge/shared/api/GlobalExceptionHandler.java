package com.supporthub.knowledge.shared.api;

import com.supporthub.knowledge.draft.persistence.IllegalDraftStateException;
import com.supporthub.knowledge.security.AccessDeniedException;
import com.supporthub.knowledge.shared.error.BusinessValidationException;
import com.supporthub.knowledge.shared.error.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiErrorResponse> notFound(ResourceNotFoundException exception, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiErrorResponse> forbidden(AccessDeniedException exception, HttpServletRequest request) {
        return error(HttpStatus.FORBIDDEN, exception.getMessage(), request);
    }

    @ExceptionHandler(BusinessValidationException.class)
    ResponseEntity<ApiErrorResponse> badRequest(BusinessValidationException exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(IllegalDraftStateException.class)
    ResponseEntity<ApiErrorResponse> illegalDraftState(IllegalDraftStateException exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> validation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        var message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("Request validation failed");
        return error(HttpStatus.BAD_REQUEST, message, request);
    }

    private ResponseEntity<ApiErrorResponse> error(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        ));
    }
}
