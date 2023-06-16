package com.example.locationsystem.location;

import java.util.List;

public interface LocationService {

    void saveLocation(Location location);

    Location findLocationByNameAndUserId(String name, Long userId);

    List<Location> findAllAddedLocations(Long id);

    List<Location> findAllLocationsWithAccess(Long id, String title);

    List<Location> findNotSharedToUserLocations(Long id, Long userId);

    void deleteLocation(Long id, Long userId);

}
