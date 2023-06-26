package com.example.locationsystem.user;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.CompletableFuture;

import com.example.locationsystem.user.UserControllerExceptions.*;

@RestController
@Log4j2
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {

        this.userService = userService;
    }

    private boolean isValidEmail(String email) {

        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }

    @PostMapping("/registration")
    public CompletableFuture<ResponseEntity<User>> registerPost(@RequestBody User user) {

        log.info("Registration request received for user: {}", user.getUsername());
        return userService.findByUsername(user.getUsername())
            .thenApplyAsync(existingUser -> {
                if (existingUser != null) {
                    log.warn("Registration failed. User {} already exists", user.getUsername());
                    throw new AlreadyExistsException("User already exists");
                } else if (user.getUsername().isEmpty() || user.getPassword().isEmpty()) {
                    log.warn("Registration failed. Empty fields");
                    throw new EmptyFieldException("Fields can not be empty");
                } else if (!isValidEmail(user.getUsername())) {
                    log.warn("Registration failed. Invalid email format: {}", user.getUsername());
                    throw new InvalidEmailException("Invalid email format");
                } else {
                    userService.saveUser(user);
                    log.info("Registration successful for user: {}", user.getUsername());
                    return ResponseEntity.ok(user);
                }
            });
    }

    @PostMapping("/login")
    public CompletableFuture<ResponseEntity<User>> loginPost(@RequestBody User user, HttpServletResponse response) {

        log.info("Login request received for user: {}", user.getUsername());
        return userService.findUserByUsernameAndPassword(user.getUsername(), user.getPassword())
            .thenApplyAsync(existingUser -> {
                if (existingUser == null) {
                    log.warn("Login failed. Invalid login or password for user: {}", user.getUsername());
                    throw new InvalidLoginOrPasswordException("Invalid login or password");
                }
                Cookie cookie = new Cookie("user", existingUser.getId().toString());
                cookie.setPath("/");
                response.addCookie(cookie);
                log.info("Login successful for user: {}", user.getUsername());
                return ResponseEntity.ok(user);
            });
    }

    @GetMapping("/logout")
    public CompletableFuture<ResponseEntity<String>> logout(HttpServletRequest request, HttpServletResponse response) {

        log.info("Logout request received");
        return CompletableFuture.supplyAsync(() -> {
            Cookie cookie = WebUtils.getCookie(request, "user");
            if (cookie == null) {
                log.warn("Logout failed. User not logged in");
                throw new NotLoggedInException("Not logged in");
            }
            cookie.setMaxAge(0);
            response.addCookie(cookie);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("message", "Logged out successfully");
            log.info("Logout successful");
            return ResponseEntity.ok().headers(headers).build();
        });
    }
}
