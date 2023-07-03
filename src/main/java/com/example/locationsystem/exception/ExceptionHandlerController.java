package com.example.locationsystem.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

import com.example.locationsystem.exception.ControllerExceptions.*;

@RestControllerAdvice
public class ExceptionHandlerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandlerController.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Void> handleValidationException(MethodArgumentNotValidException ex) {

        List<ObjectError> errors = ex.getBindingResult().getAllErrors();
        String errorMessage = errors.stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .collect(Collectors.joining(", "));
        LOGGER.warn("Failed: {}", errorMessage);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("errorMessage", errorMessage);
        return ResponseEntity.badRequest().headers(headers).build();
    }

    @ExceptionHandler({AlreadyExistsException.class,
        InvalidLoginOrPasswordException.class, NotLoggedInException.class, LocationOwnerNotFoundException.class,
        NoLocationFoundException.class, UserAccessNotFoundException.class, NoUserToShareException.class, SelfShareException.class})
    public ResponseEntity<Void> handleControllerException(RuntimeException e) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("errorMessage", e.getMessage());
        return ResponseEntity.badRequest().headers(headers).build();
    }
}

