package com.vinsguru.netflux.movie.advice;

import com.vinsguru.netflux.movie.exceptions.MovieNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String NOT_FOUND = "Not Found";
    private static final String BAD_REQUEST = "Bad Request";
    private static final String VALIDATION_FAILED = "Validation failed";
    private static final String INVALID_REQUEST_PARAMETER = "Invalid request parameter";

    @ExceptionHandler(MovieNotFoundException.class)
    public ProblemDetail handleNotFound(MovieNotFoundException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle(NOT_FOUND);
        return problem;
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HandlerMethodValidationException.class})
    public ProblemDetail handleValidation(Exception ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, VALIDATION_FAILED);
        problem.setTitle(BAD_REQUEST);
        return problem;
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ProblemDetail handleBadRequest(Exception ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, INVALID_REQUEST_PARAMETER);
        problem.setTitle(BAD_REQUEST);
        return problem;
    }
}
