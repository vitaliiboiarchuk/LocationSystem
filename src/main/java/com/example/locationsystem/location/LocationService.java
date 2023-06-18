package com.example.locationsystem.location;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface LocationService {

    CompletableFuture<Void> saveLocation(Location location);

    CompletableFuture<Location> findLocationByNameAndUserId(String name, Long userId);

    CompletableFuture<List<Location>> findAllAddedLocations(Long id);

    CompletableFuture<List<Location>> findAllLocationsWithAccess(Long id, String title);

    CompletableFuture<List<Location>> findNotSharedToUserLocations(Long id, Long userId);

    CompletableFuture<Void> deleteLocation(Long id, Long userId);

}
