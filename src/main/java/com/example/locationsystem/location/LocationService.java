package com.example.locationsystem.location;

public interface LocationService {

    void saveLocation(Location location);

    Location findLocationByName(String name);

}
