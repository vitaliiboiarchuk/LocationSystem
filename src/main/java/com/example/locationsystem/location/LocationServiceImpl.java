package com.example.locationsystem.location;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class LocationServiceImpl implements LocationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocationServiceImpl.class);

    private final LocationDao locationDao;

    public LocationServiceImpl(LocationDao locationDao) {

        this.locationDao = locationDao;
    }

    @Override
    public CompletableFuture<Void> saveLocation(Location location) {

        LOGGER.info("Saving location: {}", location);
        return locationDao.saveLocation(location);
    }

    @Override
    public CompletableFuture<Location> findLocationByNameAndUserId(String name, Long userId) {

        LOGGER.info("Finding location by name and user id. Name: {}, User id: {}", name, userId);
        return locationDao.findLocationByNameAndUserId(name, userId);
    }

    @Override
    public CompletableFuture<List<Location>> findAllAddedLocations(Long id) {

        return locationDao.findAllAddedLocations(id);
    }

    @Override
    public CompletableFuture<List<Location>> findAllLocationsWithAccess(Long id, String title) {

        return locationDao.findAllLocationsWithAccess(id, title);
    }

    @Override
    public CompletableFuture<List<Location>> findNotSharedToUserLocations(Long id, Long userId) {

        LOGGER.info("Finding not shared to user locations by User id: {} and User to share id: {}", id, userId);
        return locationDao.findNotSharedToUserLocations(id, userId);
    }

    @Override
    public CompletableFuture<Void> deleteLocation(Long id, Long userId) {

        LOGGER.info("Deleting location by location id: {} and user id: {}", id, userId);
        return locationDao.deleteLocation(id, userId);
    }

    @Override
    public CompletableFuture<Location> findById(Long id) {

        return locationDao.findById(id);
    }

    @Override
    public CompletableFuture<List<Location>> findAllMyLocations(Long userId) {

        LOGGER.info("Finding all my locations by user id: {}", userId);
        CompletableFuture<List<Location>> addedLocationsFuture =
            findAllAddedLocations(userId);
        CompletableFuture<List<Location>> adminAccessFuture =
            findAllLocationsWithAccess(userId, "ADMIN");
        CompletableFuture<List<Location>> readAccessFuture =
            findAllLocationsWithAccess(userId, "READ");

        return CompletableFuture.allOf(addedLocationsFuture, adminAccessFuture, readAccessFuture)
            .thenApplyAsync((Void) -> Stream.of(
                addedLocationsFuture.join(),
                adminAccessFuture.join(),
                readAccessFuture.join()
            ).flatMap(List::stream).collect(Collectors.toList()));
    }

    @Override
    public CompletableFuture<Location> findLocationByName(String name) {

        return locationDao.findLocationByName(name);
    }
}