package com.example.locationsystem.location;

import com.example.locationsystem.event.EventService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Log4j2
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LocationServiceImpl implements LocationService {

    LocationDao locationDao;
    EventService eventService;

    @Override
    public CompletableFuture<List<Location>> findAllUserLocations(Long userId) {

        log.info("Finding all user locations by user id={}", userId);
        return locationDao.findAllUserLocations(userId);
    }

    @Override
    public CompletableFuture<Location> findLocationInUserLocations(Long userId, Long locationId) {

        log.info("Finding location in user locations by user id={} and location id={}", userId, locationId);
        return locationDao.findLocationInUserLocations(userId, locationId);
    }

    @Override
    public CompletableFuture<Optional<Location>> findLocationByNameAndUserId(String name, Long userId) {

        log.info("Finding location by name={} and user id={}", name, userId);
        return locationDao.findLocationByNameAndUserId(name, userId);
    }

    @Override
    public CompletableFuture<Location> saveLocation(Location location, Long ownerId) {

        location.setUserId(ownerId);
        log.info("Saving location={}", location);
        return locationDao.saveLocation(location)
            .thenApply(savedLocation -> {
                eventService.publishLocationCreatedEvent(savedLocation);
                return savedLocation;
            });
    }

    @Override
    public CompletableFuture<Location> findNotSharedToUserLocation(Long id, Long locId, Long userId) {

        log.info("Finding not shared to user location by owner id={}, location id={}, user to share id={}",
            id, locId, userId);
        return locationDao.findNotSharedToUserLocation(id, locId, userId);
    }

    @Override
    public CompletableFuture<Void> deleteLocation(String name, Long userId) {

        log.info("Deleting location by location name={} and user id={}", name, userId);
        return locationDao.deleteLocation(name, userId)
            .thenAccept(optionalDeletedLocation ->
                optionalDeletedLocation.ifPresent(eventService::publishLocationDeletedEvent)
            );
    }

    @Override
    public CompletableFuture<Location> findLocationById(Long id) {

        log.info("Finding location by id={}", id);
        return locationDao.findLocationById(id);
    }
}