package com.example.locationsystem.user;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.locationsystem.user.UserControllerExceptions.*;

@RestControllerAdvice
public class UserControllerAdvice {

    @ExceptionHandler({AlreadyExistsException.class, EmptyFieldException.class, InvalidEmailException.class,
        InvalidLoginOrPasswordException.class, NotLoggedInException.class})
    public ResponseEntity<String> handleUserControllerExceptions(RuntimeException e) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("errorMessage", e.getMessage());
        return ResponseEntity.badRequest().headers(headers).build();
    }
}
