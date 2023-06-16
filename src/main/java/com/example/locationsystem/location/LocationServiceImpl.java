package com.example.locationsystem.location;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;


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
        return locationDao.findNotSharedToUserLocations(id,userId);
    }

    @Override
    public CompletableFuture<Void> deleteLocation(Long id, Long userId) {
        return locationDao.deleteLocation(id,userId);
    }

}