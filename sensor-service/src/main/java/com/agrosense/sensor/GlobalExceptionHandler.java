package com.agrosense.sensor;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<AppErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        AppErrorResponse response = AppErrorResponse.of(
            "NotFoundError",
            ex.getMessage(),
            null,
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<AppErrorResponse> handleConflict(ConflictException ex, HttpServletRequest request) {
        AppErrorResponse response = AppErrorResponse.of(
            "ConflictError",
            ex.getMessage(),
            null,
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<AppErrorResponse> handleValidation(ValidationException ex, HttpServletRequest request) {
        AppErrorResponse response = AppErrorResponse.of(
            "ValidationError",
            ex.getMessage(),
            null,
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AppErrorResponse> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex, HttpServletRequest request
    ) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        String primaryMessage = errors.values().stream().findFirst().orElse("Datos de solicitud invalidos");

        AppErrorResponse response = AppErrorResponse.of(
            "ValidationError",
            primaryMessage,
            errors,
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class,
        IllegalArgumentException.class
    })
    public ResponseEntity<AppErrorResponse> handleBadRequest(Exception ex, HttpServletRequest request) {
        AppErrorResponse response = AppErrorResponse.of(
            "ValidationError",
            ex.getMessage() != null ? ex.getMessage() : "Formato de peticion invalido",
            null,
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AppErrorResponse> handleGeneral(Exception ex, HttpServletRequest request) {
        AppErrorResponse response = AppErrorResponse.of(
            "InternalServerError",
            "Ha ocurrido un error interno en el servidor",
            null,
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
