package com.example.locationsystem.location;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.locationsystem.location.LocationControllerExceptions.*;

@RestControllerAdvice
public class LocationControllerAdvice {

    @ExceptionHandler({LocationOwnerNotFoundException.class, NoLocationFoundException.class,
        UserAccessNotFoundException.class, NoUserToShareException.class})
    public ResponseEntity<String> handleLocationControllerExceptions(RuntimeException e) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("errorMessage", e.getMessage());
        return ResponseEntity.badRequest().headers(headers).build();
    }
}
