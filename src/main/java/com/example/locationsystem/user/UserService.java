package com.example.locationsystem.user;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface UserService {

    CompletableFuture<User> findByUsername(String name);

    CompletableFuture<User> findUserByUsernameAndPassword(String username, String password);

    CompletableFuture<Void> saveUser(User user);

    CompletableFuture<User> findById(Long id);

    CompletableFuture<List<User>> findAllUsersWithAccessOnLocation(Long locationId, Long userId);

    CompletableFuture<User> findLocationOwner(Long locationId, Long id);

    CompletableFuture<Void> deleteUserByUsername(String username);

    Long getMaxIdFromUsers();
}
