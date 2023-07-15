package com.example.locationsystem.user;

import com.example.locationsystem.annotation.GetAndValidUserId;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import java.util.concurrent.CompletableFuture;

import com.example.locationsystem.exception.ControllerExceptions.*;
import org.springframework.web.util.WebUtils;

@RestController
@Log4j2
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {

        this.userService = userService;
    }

    @PostMapping("/registration")
    public CompletableFuture<ResponseEntity<User>> registerPost(@Valid @RequestBody User user) {

        return userService.findUserByEmail(user.getUsername())
            .thenCompose(existingUser -> {
                if (existingUser != null) {
                    log.warn("Registration failed. User {} already exists", user.getUsername());
                    throw new AlreadyExistsException("User already exists");
                }
                return userService.saveUser(user)
                    .thenApply(savedUser -> {
                        log.info("Registration successful for user: {}", user.getUsername());
                        return ResponseEntity.ok(savedUser);
                    });
            });
    }

    @PostMapping("/login")
    public CompletableFuture<ResponseEntity<User>> loginPost(@RequestBody User user, HttpServletResponse response) {

        return userService.findUserByEmailAndPassword(user.getUsername(), user.getPassword())
            .thenApply(existingUser -> {
                if (existingUser == null) {
                    log.warn("Login failed. Invalid login or password");
                    throw new InvalidLoginOrPasswordException("Invalid login or password");
                }
                Cookie cookie = new Cookie("user", existingUser.getId().toString());
                cookie.setPath("/");
                response.addCookie(cookie);
                log.info("Login successful for user: {}", user.getUsername());
                return ResponseEntity.ok(existingUser);
            });
    }

    @DeleteMapping("/delete/{email}")
    public CompletableFuture<ResponseEntity<Void>> deleteUser(@PathVariable String email) {

        return userService.deleteUserByEmail(email)
            .thenApply(deleted -> {
                log.info("User deleted successfully. User username: {}", email);
                HttpHeaders headers = new HttpHeaders();
                headers.add("message", "User deleted successfully");
                return ResponseEntity.ok().headers(headers).build();
            });
    }
}
