package com.example.locationsystem.userAccess;

import java.util.concurrent.CompletableFuture;

public interface UserAccessService {

    CompletableFuture<UserAccess> saveUserAccess(UserAccess userAccess);

    CompletableFuture<UserAccess> changeUserAccess(UserAccess userAccess, Long ownerId);
}
