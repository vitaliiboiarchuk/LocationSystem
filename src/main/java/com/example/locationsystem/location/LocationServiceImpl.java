package com.example.locationsystem.location;

import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class LocationServiceImpl implements LocationService {

    private final LocationDao locationDao;

    public LocationServiceImpl(LocationDao locationDao) {
        this.locationDao = locationDao;
    }


    @Override
    public void saveLocation(Location location) {
        locationDao.saveLocation(location);
    }

    @Override
    public Location findLocationByNameAndUserId(String name, Long userId) {
        return locationDao.findLocationByNameAndUserId(name,userId);
    }

    @Override
    public List<Location> findAllAddedLocations(Long id) {
        return locationDao.findAllAddedLocations(id);
    }

    @Override
    public List<Location> findAllLocationsWithAccess(Long id, String title) {
        return locationDao.findAllLocationsWithAccess(id,title);
    }

    @Override
    public List<Location> findNotSharedToUserLocations(Long id, Long userId) {
        return locationDao.findNotSharedToUserLocations(id,userId);
    }

    @Override
    public void deleteLocation(Long id, Long userId) {
        locationDao.deleteLocation(id,userId);
    }

}