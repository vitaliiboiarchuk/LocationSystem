package com.example.locationsystem.user;

import com.example.locationsystem.util.EmailUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import java.util.concurrent.CompletableFuture;

import com.example.locationsystem.exception.ControllerExceptions.*;

@RestController
@Log4j2
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;
    EmailUtil emailUtil;

    @PostMapping("/registration")
    public CompletableFuture<ResponseEntity<Long>> registerPost(@Valid @RequestBody User user) {

        return userService.findUserByEmail(user.getUsername())
            .thenCompose(existingUser -> {
                if (existingUser.isPresent()) {
                    log.warn("Registration failed. User with email={} already exists",
                        emailUtil.hideEmail(user.getUsername()));
                    throw new AlreadyExistsException("User already exists");
                }
                return userService.saveUser(user)
                    .thenApply(ResponseEntity::ok);
            });
    }

    @PostMapping("/login")
    public CompletableFuture<ResponseEntity<Long>> loginPost(@RequestBody User user, HttpServletResponse response) {

        return userService.findUserByEmailAndPassword(user.getUsername(), user.getPassword())
            .thenApply(existingUser -> {
                Cookie cookie = new Cookie("user", existingUser.getId().toString());
                cookie.setPath("/");
                response.addCookie(cookie);
                return ResponseEntity.ok(existingUser.getId());
            });
    }

    @DeleteMapping("/delete/{email}")
    public CompletableFuture<ResponseEntity<Void>> deleteUser(@PathVariable String email) {

        return userService.deleteUserByEmail(email)
            .thenApply(deleted -> ResponseEntity.ok().build());
    }
}
