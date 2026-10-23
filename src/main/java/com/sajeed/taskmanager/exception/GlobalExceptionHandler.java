package com.sajeed.taskmanager.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ApiError(
        Instant timestamp,
        int status,
        String message,
        String path,
        Map<String, String> errors
    ) {}

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
            TaskNotFoundException exception,
            HttpServletRequest request) {
        return error(
            HttpStatus.NOT_FOUND,
            exception.getMessage(),
            request,
            Map.of()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        Map<String, String> fields = new LinkedHashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(
            field -> fields.putIfAbsent(
                field.getField(),
                field.getDefaultMessage() == null
                    ? "Invalid value"
                    : field.getDefaultMessage()
            )
        );

        return error(
            HttpStatus.BAD_REQUEST,
            "Validation failed",
            request,
            fields
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleInvalidBody(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {
        return error(
            HttpStatus.BAD_REQUEST,
            "Invalid request body. Use valid JSON and correct field types. "
                + "Status: TODO, IN_PROGRESS, DONE. "
                + "Priority: LOW, MEDIUM, HIGH.",
            request,
            Map.of()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleInvalidParameter(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request) {
        return error(
            HttpStatus.BAD_REQUEST,
            "Invalid parameter '" + exception.getName()
                + "': expected a UUID.",
            request,
            Map.of()
        );
    }

    private ResponseEntity<ApiError> error(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            Map<String, String> errors) {

        ApiError body = new ApiError(
            Instant.now(),
            status.value(),
            message,
            request.getRequestURI(),
            errors
        );

        return ResponseEntity.status(status).body(body);
    }
}