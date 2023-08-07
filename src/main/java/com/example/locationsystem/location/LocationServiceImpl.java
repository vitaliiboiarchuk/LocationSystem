package com.example.locationsystem.location;

import com.example.locationsystem.event.ObjectChangeEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.example.locationsystem.exception.ControllerExceptions.*;

@Service
@Log4j2
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LocationServiceImpl implements LocationService {

    LocationDao locationDao;
    ApplicationEventPublisher eventPublisher;

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
                eventPublisher.publishEvent(new ObjectChangeEvent(this, ObjectChangeEvent.ObjectType.LOCATION,
                    ObjectChangeEvent.ActionType.CREATED,
                    new Timestamp(System.currentTimeMillis()), savedLocation.getId()));
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
        return locationDao.findLocationByNameAndUserId(name, userId)
            .thenCompose(locationOptional -> {
                if (locationOptional.isPresent()) {
                    return locationDao.deleteLocation(name, userId)
                        .thenAccept(result ->
                            eventPublisher.publishEvent(new ObjectChangeEvent(this,
                                ObjectChangeEvent.ObjectType.LOCATION, ObjectChangeEvent.ActionType.DELETED,
                                new Timestamp(System.currentTimeMillis()), locationOptional.get().getId())));
                } else {
                    log.warn("Location not found by name={} and user id={}", name, userId);
                    throw new LocationNotFoundException("Location not found");
                }
            });
    }

    @Override
    public CompletableFuture<Location> findLocationById(Long id) {

        log.info("Finding location by id={}", id);
        return locationDao.findLocationById(id);
    }
}