package com.example.locationsystem.location

import com.example.locationsystem.exception.ControllerExceptions
import org.springframework.context.ApplicationEventPublisher
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.CompletableFuture


class LocationServiceTest extends Specification {

    LocationDao locationDao

    LocationService locationService

    ApplicationEventPublisher eventPublisher

    @Shared
    def loc = new Location(name: "name1", address: "add1", userId: 1L)
    List<Location> locs

    def setup() {

        locationDao = Mock(LocationDao)
        eventPublisher = Mock(ApplicationEventPublisher)

        locationService = new LocationServiceImpl(locationDao,eventPublisher)

        locs = new ArrayList()
        locs << loc
    }

    def "saveLocation should insert location into database"() {

        given:
            def locationToSave = Stub(Location)

        and:
            def expectedLocation = Stub(Location)

        when:
            def result = locationService.saveLocation(locationToSave, 100L).join()

        then:
            result == expectedLocation

        then:
            1 * locationDao.saveLocation(locationToSave) >> CompletableFuture.completedFuture(expectedLocation)
            1 * eventPublisher.publishEvent(_) >> null
    }

    def "findLocationByNameAndUserId should return location"() {

        given:
            locationDao.findLocationByNameAndUserId(loc.getName(), 1) >> CompletableFuture.completedFuture(Optional.of(loc))

        when:
            def result = locationService.findLocationByNameAndUserId(loc.getName(), 1)

        then:
            result.get().isPresent()
            result.get().get().getName() == loc.getName()
    }

    def "findAllUserLocations should return locations"() {

        given:
            locationDao.findAllUserLocations(1) >> CompletableFuture.completedFuture(locs)

        when:
            def result = locationService.findAllUserLocations(1)

        then:
            result.get() == locs
    }

    def "findLocationInUserLocations should return location"() {

        given:
            locationDao.findLocationInUserLocations(1, 1) >> CompletableFuture.completedFuture(loc)

        when:
            def result = locationService.findLocationInUserLocations(1, 1)

        then:
            result.get() == loc
    }

    def "should delete location"() {

        when:
            locationService.deleteLocation("name1", 1)

        then:
            1 * locationDao.findLocationByNameAndUserId("name1", 1) >> CompletableFuture.completedFuture(Optional.of(loc))
            1 * locationDao.deleteLocation("name1", 1) >> CompletableFuture.completedFuture(null)
            1 * eventPublisher.publishEvent(_) >> null
    }

    def "should throw LocationNotFoundException when location not found"() {

        when:
            CompletableFuture<Void> result = locationService.deleteLocation("name", 100)

        then:
            1 * locationDao.findLocationByNameAndUserId("name", 100) >> CompletableFuture.completedFuture(Optional.empty())
            0 * locationDao.deleteLocation("name", 100)
            0 * eventPublisher.publishEvent(_)

        and:
            try {
                result.get()
            } catch (Exception e) {
                Throwable cause = e.getCause()
                cause instanceof ControllerExceptions.LocationNotFoundException
            }
    }

    def "should find not shared locations to user"() {

        given:
            locationDao.findNotSharedToUserLocation(1, 2, 2) >> CompletableFuture.completedFuture(loc)

        when:
            def result = locationService.findNotSharedToUserLocation(1, 2, 2)

        then:
            result.get() == loc
    }

    def "should find location by id"() {

        given:
            locationDao.findLocationById(loc.getId()) >> CompletableFuture.completedFuture(loc)

        when:
            def result = locationService.findLocationById(loc.getId())

        then:
            result.get() == loc
    }
}
