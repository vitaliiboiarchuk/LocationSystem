package com.example.locationsystem.location;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Log4j2
public class LocationServiceImpl implements LocationService {

    private final LocationDao locationDao;

    public LocationServiceImpl(LocationDao locationDao) {

        this.locationDao = locationDao;
    }

    @Override
    public CompletableFuture<List<Location>> findAllUserLocations(Long userId) {

        log.info("Finding all user locations by user id: {}", userId);
        return locationDao.findAllUserLocations(userId);
    }

    @Override
    public CompletableFuture<Location> findLocationByNameAndUserId(String name, Long userId) {

        log.info("Finding location by name and user id. Name: {}, User id: {}", name, userId);
        return locationDao.findLocationByNameAndUserId(name, userId);
    }

    @Override
    public CompletableFuture<Location> saveLocation(Location location, Long ownerId) {

        location.setUserId(ownerId);
        return locationDao.saveLocation(location);
    }

        @Override
    public CompletableFuture<Location> findLocationById(Long id) {

        return locationDao.findLocationById(id);
    }

    @Override
    public CompletableFuture<List<Location>> findNotSharedToUserLocations(Long id, Long userId) {

        log.info("Finding not shared to user locations by User id: {} and User to share id: {}", id, userId);
        return locationDao.findNotSharedToUserLocations(id, userId);
    }

    @Override
    public CompletableFuture<Void> deleteLocation(Long id, Long userId) {

        log.info("Deleting location by location id: {} and user id: {}", id, userId);
        return locationDao.deleteLocation(id, userId);
    }

}