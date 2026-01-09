package com.itdev.finalproject.http.handler;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
            body.setTitle("Ошибка валидации запроса");
            body.setDetail(detail);
        }

        return objectResponseEntity;
    }
}
