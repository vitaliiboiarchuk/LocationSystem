package com.example.locationsystem.userAccess;

import java.util.concurrent.CompletableFuture;

public interface UserAccessService {

    CompletableFuture<Void> saveUserAccess(UserAccess userAccess);

    CompletableFuture<Void> changeUserAccess(Long locationId, Long userId);
}
