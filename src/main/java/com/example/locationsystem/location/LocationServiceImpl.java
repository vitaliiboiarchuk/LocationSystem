package com.example.locationsystem.location;

import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Override
    public List<Location> findAllAddedLocations(Long id) {
        return locationRepository.findLocationsByUserId(id);
    }

    @Override
    public List<Location> findAllLocationsWithAccess(Long id, String title) {
        return locationRepository.findAllAccessLocationsByUserIdAndTitle(id, title);
    }

    @Override
    public List<Location> findNotSharedToUserLocations(Long id, Long userId) {
        List<Location> locationsToShare = Stream.of(
                        findAllAddedLocations(id),
                        findAllLocationsWithAccess(id,"ADMIN")
                )
                .flatMap(Collection::stream).collect(Collectors.toList());

        List<Location> allLocationsOfUser = Stream.of(
                        findAllAddedLocations(userId),
                        findAllLocationsWithAccess(userId,"ADMIN"),
                        findAllLocationsWithAccess(userId,"READ")
                )
                .flatMap(Collection::stream).collect(Collectors.toList());

        for (Location location : allLocationsOfUser) {
            locationsToShare.remove(location);
        }
        return locationsToShare;
    }

    @Override
    public void deleteLocation(Long id) {
        locationRepository.deleteById(id);
    }

}