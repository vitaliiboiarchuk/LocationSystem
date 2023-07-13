package com.example.locationsystem.location;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface LocationService {

    CompletableFuture<List<Location>> findAllUserLocations(Long userId);

    CompletableFuture<Location> findLocationInUserLocations(Long userId, Long locationId);

    CompletableFuture<Location> findLocationByNameAndUserId(String name, Long userId);

    CompletableFuture<Location> saveLocation(Location location, Long ownerId);

    CompletableFuture<Location> findLocationById(Long id);

    CompletableFuture<Location> findNotSharedToUserLocation(Long id, Long locId, Long userId);

    CompletableFuture<Void> deleteLocation(Long id, Long userId);

}
