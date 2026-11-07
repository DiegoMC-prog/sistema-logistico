package com.tienda.backend.shared.infrastructure.adapter.in.rest;

import com.tienda.backend.shared.domain.exception.BusinessConflictException;
import com.tienda.backend.shared.domain.exception.InvalidBusinessRuleException;
import com.tienda.backend.shared.domain.exception.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(BusinessConflictException.class)
    public ProblemDetail handleConflict(BusinessConflictException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(InvalidBusinessRuleException.class)
    public ProblemDetail handleInvalidRule(InvalidBusinessRuleException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Red de seguridad ante concurrencia: dos peticiones pasan la validación previa
    // y la restricción única/FK de la BD rechaza la segunda.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "La operación viola una restricción de datos (registro duplicado o referencia inválida)");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, List<String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        LinkedHashMap::new,
                        Collectors.mapping(e -> String.valueOf(e.getDefaultMessage()), Collectors.toList())));

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Datos de entrada inválidos");
        problem.setProperty("errors", errors);
        return problem;
    }
}
