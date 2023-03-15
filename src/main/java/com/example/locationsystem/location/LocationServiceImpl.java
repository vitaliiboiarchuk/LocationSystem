package com.example.locationsystem.location;

import org.springframework.stereotype.Service;

@Service
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;

    public LocationServiceImpl(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Override
    public void saveLocation(Location location) {
        locationRepository.save(location);
    }

    @Override
    public Location findLocationByName(String name) {
        return locationRepository.findLocationsByName(name);
    }

}