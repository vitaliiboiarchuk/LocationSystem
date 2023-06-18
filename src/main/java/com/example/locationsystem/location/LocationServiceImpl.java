package com.example.locationsystem.location;

import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class LocationServiceImpl implements LocationService {

    private final LocationDao locationDao;

    public LocationServiceImpl(LocationDao locationDao) {
        this.locationDao = locationDao;
    }


    @Override
    public CompletableFuture<Void> saveLocation(Location location) {
        return locationDao.saveLocation(location);
    }

    @Override
    public CompletableFuture<Location> findLocationByNameAndUserId(String name, Long userId) {
        return locationDao.findLocationByNameAndUserId(name,userId);
    }

    @Override
    public CompletableFuture<List<Location>> findAllAddedLocations(Long id) {
        return locationDao.findAllAddedLocations(id);
    }

    @Override
    public CompletableFuture<List<Location>> findAllLocationsWithAccess(Long id, String title) {
        return locationDao.findAllLocationsWithAccess(id,title);
    }

    @Override
    public CompletableFuture<List<Location>> findNotSharedToUserLocations(Long id, Long userId) {
        CompletableFuture<List<Location>> addedLocationsFuture = findAllAddedLocations(id);
        CompletableFuture<List<Location>> adminLocationsFuture = findAllLocationsWithAccess(id, "ADMIN");

        CompletableFuture<List<Location>> userAddedLocationsFuture = findAllAddedLocations(userId);
        CompletableFuture<List<Location>> userAdminLocationsFuture = findAllLocationsWithAccess(userId, "ADMIN");
        CompletableFuture<List<Location>> userReadLocationsFuture = findAllLocationsWithAccess(userId, "READ");

        return CompletableFuture.allOf(addedLocationsFuture, adminLocationsFuture, userAddedLocationsFuture, userReadLocationsFuture, userAdminLocationsFuture)
                .thenApplyAsync((Void) -> {
                    List<Location> locationsToShare =
                            Stream.of(addedLocationsFuture.join(), adminLocationsFuture.join())
                                    .flatMap(Collection::stream)
                                    .collect(Collectors.toList());
                    List<Location> allLocationsOfUser =
                            Stream.of(userAddedLocationsFuture.join(), userAdminLocationsFuture.join(), userReadLocationsFuture.join())
                                    .flatMap(Collection::stream)
                                    .collect(Collectors.toList());

                    allLocationsOfUser.forEach(locationsToShare::remove);

                    return locationsToShare;
                });
    }

    @Override
    public CompletableFuture<Void> deleteLocation(Long id, Long userId) {
        return locationDao.deleteLocation(id,userId);
    }

}