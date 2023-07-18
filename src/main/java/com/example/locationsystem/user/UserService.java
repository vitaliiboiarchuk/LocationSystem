package com.example.locationsystem.user;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface UserService {

    CompletableFuture<User> findUserByEmail(String email);

    CompletableFuture<User> findUserByEmailAndPassword(String email, String password);

    CompletableFuture<User> saveUser(User user);

    CompletableFuture<User> findUserById(Long id);

    CompletableFuture<List<User>> findAllUsersOnLocation(Long locationId, Long userId);

    CompletableFuture<User> findLocationOwner(Long locationId);

    CompletableFuture<Void> deleteUserByEmail(String email);

}
