package com.tienda.backend.shared.infrastructure.adapter.in.rest;

import com.tienda.backend.shared.domain.exception.AccesoDenegadoException;
import com.tienda.backend.shared.domain.exception.AutenticacionException;
import com.tienda.backend.shared.domain.exception.BusinessConflictException;
import com.tienda.backend.shared.domain.exception.InvalidBusinessRuleException;
import com.tienda.backend.shared.domain.exception.ResourceNotFoundException;
import com.tienda.backend.shared.domain.exception.ServicioExternoException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
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

    @ExceptionHandler(AccesoDenegadoException.class)
    public ProblemDetail handleAccesoDenegado(AccesoDenegadoException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(AutenticacionException.class)
    public ProblemDetail handleAutenticacion(AutenticacionException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(ServicioExternoException.class)
    public ProblemDetail handleServicioExterno(ServicioExternoException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, ex.getMessage());
    }

    // Red de seguridad ante concurrencia: dos peticiones pasan la validación previa
    // y la restricción única/FK de la BD rechaza la segunda.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "La operación viola una restricción de datos (registro duplicado o referencia inválida)");
    }

    // Esperar un bloqueo de stock superó el tiempo máximo, o la BD detectó un interbloqueo entre dos operaciones.
    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ProblemDetail handleBloqueoPesimista(PessimisticLockingFailureException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "El recurso está ocupado por otra operación; vuelva a intentarlo en unos segundos");
    }

    // Dos operaciones modificaron el mismo registro a la vez (p. ej. el stock de un lote): gana la primera.
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail handleBloqueoOptimista(ObjectOptimisticLockingFailureException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "El registro fue modificado por otra operación al mismo tiempo; vuelva a intentarlo");
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

    // Cuerpo JSON ilegible o con un valor que no corresponde al tipo (p. ej. un enum inexistente). Sin este manejo
    // Spring redirige a /error, que no está autenticado, y el cliente recibiría un engañoso 401.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleCuerpoIlegible(HttpMessageNotReadableException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "El cuerpo de la petición es inválido: revise el formato JSON y los valores permitidos");
    }

    // Parámetro de ruta o de consulta con un valor que no corresponde al tipo (p. ej. ?estado=INEXISTENTE).
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleParametroInvalido(MethodArgumentTypeMismatchException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "El valor del parámetro '" + ex.getName() + "' no es válido");
    }
}
