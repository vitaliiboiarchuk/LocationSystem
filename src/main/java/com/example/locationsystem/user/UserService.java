package com.example.locationsystem.user;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface UserService {

    CompletableFuture<Optional<User>> findUserByEmail(String email);

    CompletableFuture<User> findUserByEmailAndPassword(String email, String password);

    CompletableFuture<User> saveUser(User user);

    CompletableFuture<List<User>> findAllUsersOnLocation(Long locationId, Long userId);

    CompletableFuture<Void> deleteUserByEmail(String email);

    CompletableFuture<User> findUserById(Long id);

}
