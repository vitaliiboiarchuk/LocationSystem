package com.example.locationsystem.userAccess;

import java.util.concurrent.CompletableFuture;

public interface UserAccessService {

    CompletableFuture<UserAccess> saveUserAccess(UserAccess userAccess);

    CompletableFuture<Void> changeUserAccess(UserAccess userAccess);

    CompletableFuture<UserAccess> findUserAccess(UserAccess userAccess, Long userId);
}
