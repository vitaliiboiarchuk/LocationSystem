package com.example.locationsystem.location;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface LocationService {

    CompletableFuture<List<Location>> findAllUserLocations(Long userId);

    CompletableFuture<Location> findLocationInUserLocations(Long userId, Long locationId);

    CompletableFuture<Optional<Location>> findLocationByNameAndUserId(String name, Long userId);

    CompletableFuture<Location> saveLocation(Location location, Long ownerId);

    CompletableFuture<Location> findNotSharedToUserLocation(Long id, Long locId, Long userId);

    CompletableFuture<Void> deleteLocation(String name, Long userId);

}
