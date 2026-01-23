package com.itdev.finalproject.http.handler;

import com.itdev.finalproject.dto.ServerErrorDto;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

@RestControllerAdvice(basePackages = "com.itdev.finalproject.http.rest")
public class RestControllerExceptionHandler extends ResponseEntityExceptionHandler {


    // Обработка валидации (@Valid)
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        ResponseEntity<Object> objectResponseEntity = super.handleMethodArgumentNotValid(ex, headers, status, request);

        String detail = ex.getBindingResult()
                .getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        if (objectResponseEntity != null
                && objectResponseEntity.getBody() instanceof ProblemDetail body) {
            body.setTitle("Request validation error");
            body.setDetail(detail);
        }

        return objectResponseEntity;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ServerErrorDto> illegalArgumentExceptionHandle(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(new ServerErrorDto(
                        "Request validation error",
                        e.getMessage()
                ));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ServerErrorDto> illegalArgumentExceptionHandle(IllegalStateException e) {
        return ResponseEntity.badRequest()
                .body(new ServerErrorDto(
                        "Bad request error",
                        e.getMessage()
                ));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ServerErrorDto> illegalArgumentExceptionHandle(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ServerErrorDto(
                        "Entity Not Found",
                        e.getMessage()
                ));
    }
}
