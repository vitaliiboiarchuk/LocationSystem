package com.example.locationsystem.user;

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

@RestController
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
    public CompletableFuture<ResponseEntity<String>> registerPost(@RequestBody User user) {

        CompletableFuture<User> userFuture = userService.findByUsername(user.getUsername());

        return userFuture.thenApplyAsync(existingUser -> {
            if (existingUser != null) {
                return ResponseEntity.badRequest().body("User already exists");
            } else if (user.getUsername().isEmpty() || user.getPassword().isEmpty()) {
                return ResponseEntity.badRequest().body("Fields username and password can not be empty");
            } else if (!isValidEmail(user.getUsername())) {
                return ResponseEntity.badRequest().body("Invalid email format");
            } else {
                userService.saveUser(user);
                return ResponseEntity.ok("User registered successfully");
            }
        });
    }

    @PostMapping("/login")
    public CompletableFuture<ResponseEntity<String>> loginPost(@RequestBody User user, HttpServletResponse response) {

        CompletableFuture<User> userFuture = userService.findUserByUsernameAndPassword(user.getUsername(),
            user.getPassword());

        return userFuture.thenApplyAsync(existingUser -> {
            if (existingUser != null) {
                Cookie cookie = new Cookie("user", existingUser.getId().toString());
                cookie.setPath("/");
                response.addCookie(cookie);
                return ResponseEntity.ok("Logged in successfully");
            } else {
                return ResponseEntity.badRequest().body("Log in failed");
            }
        });
    }

    @GetMapping("/logout")
    public CompletableFuture<ResponseEntity<String>> logout(HttpServletRequest request, HttpServletResponse response) {

        return CompletableFuture.supplyAsync(() -> {
            Cookie cookie = WebUtils.getCookie(request, "user");
            if (cookie != null) {
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
            return ResponseEntity.ok("Logged out successfully");
        });
    }
}
