package com.example.locationsystem.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.concurrent.CompletableFuture;

import com.example.locationsystem.exception.ControllerExceptions.*;

@RestController
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {

        this.userService = userService;
    }

    @PostMapping("/registration")
    public CompletableFuture<ResponseEntity<User>> registerPost(@Valid @RequestBody User user) {

        LOGGER.info("Registration request received for user: {}", user.getUsername());
        return userService.findByUsername(user.getUsername())
            .thenApplyAsync(existingUser -> {
                if (existingUser != null) {
                    LOGGER.warn("Registration failed. User {} already exists", user.getUsername());
                    throw new AlreadyExistsException("User already exists");
                } else {
                    userService.saveUser(user);
                    LOGGER.info("Registration successful for user: {}", user.getUsername());
                    return ResponseEntity.ok(user);
                }
            });
    }

    @PostMapping("/login")
    public CompletableFuture<ResponseEntity<User>> loginPost(@RequestBody User user, HttpServletResponse response) {

        LOGGER.info("Login request received for user: {}", user.getUsername());
        return userService.findUserByUsernameAndPassword(user.getUsername(), user.getPassword())
            .thenApplyAsync(existingUser -> {
                if (existingUser == null) {
                    LOGGER.warn("Login failed. Invalid login or password for user: {}", user.getId());
                    throw new InvalidLoginOrPasswordException("Invalid login or password");
                }
                Cookie cookie = new Cookie("user", existingUser.getId().toString());
                cookie.setPath("/");
                response.addCookie(cookie);
                LOGGER.info("Login successful for user: {}", user.getUsername());
                return ResponseEntity.ok(user);
            });
    }

    @GetMapping("/logout")
    public CompletableFuture<ResponseEntity<Void>> logout(HttpServletRequest request, HttpServletResponse response) {

        LOGGER.info("Logout request received");
        return CompletableFuture.supplyAsync(() -> {
            Cookie cookie = WebUtils.getCookie(request, "user");
            if (cookie == null) {
                LOGGER.warn("Logout failed. User not logged in");
                throw new NotLoggedInException("Not logged in");
            }
            cookie.setMaxAge(0);
            response.addCookie(cookie);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("message", "Logged out successfully");

            LOGGER.info("Logout successful");
            return ResponseEntity.ok().headers(headers).build();
        });
    }

    @DeleteMapping("/delete/{username}")
    public CompletableFuture<ResponseEntity<Void>> deleteUser(@PathVariable String username) {

        LOGGER.info("Delete user request received. User username: {}", username);

        userService.deleteUserByUsername(username);
        LOGGER.info("User deleted successful. User username: {}", username);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("message", "User deleted successfully");
        return CompletableFuture.completedFuture(ResponseEntity.ok().headers(headers).build());
    }
}
