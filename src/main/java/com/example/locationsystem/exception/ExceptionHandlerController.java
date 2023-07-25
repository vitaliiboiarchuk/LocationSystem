package com.example.locationsystem.exception;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

import com.example.locationsystem.exception.ControllerExceptions.*;

@RestControllerAdvice
@Log4j2
public class ExceptionHandlerController {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Void> handleValidationException(MethodArgumentNotValidException ex) {

        List<ObjectError> errors = ex.getBindingResult().getAllErrors();
        String errorMessage = errors.stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .collect(Collectors.joining(", "));
        log.warn("Failed: {}", errorMessage);

        HttpHeaders headers = new HttpHeaders();
        headers.add("errorMessage", errorMessage);
        return ResponseEntity.badRequest().headers(headers).build();
    }

    @ExceptionHandler({AlreadyExistsException.class,
        InvalidLoginOrPasswordException.class, LocationNotFoundException.class, UserNotFoundException.class,
        LocationOrUserNotFoundException.class, UserAccessNotFoundException.class, NotLoggedInException.class})
    public ResponseEntity<Void> handleControllerException(RuntimeException e) {

        HttpHeaders headers = new HttpHeaders();
        headers.add("errorMessage", e.getMessage());
        return ResponseEntity.badRequest().headers(headers).build();
    }
}

