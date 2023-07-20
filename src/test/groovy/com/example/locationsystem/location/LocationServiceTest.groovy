package com.example.locationsystem.location

import spock.lang.Specification
import spock.lang.Subject

import java.util.concurrent.CompletableFuture

class LocationServiceTest extends Specification {

    LocationDao locationDao

    @Subject
    LocationService locationService

    Location loc
    List<Location> locs

    def setup() {

        locationDao = Mock(LocationDao)
        locationService = new LocationServiceImpl(locationDao)

        loc = new Location("name1", "add1", 1L)

        locs = new ArrayList()
        locs << loc
    }

    def "saveLocation should insert location into database"() {

        given:
            locationDao.saveLocation(loc) >> CompletableFuture.completedFuture(null)

        when:
            def result = locationService.saveLocation(loc, 1L)

        then:
            def saveResult = result?.get()
            saveResult == null
            1 * locationDao.saveLocation(loc)
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

        given:
            locationDao.deleteLocation(loc.getName(), 1) >> CompletableFuture.completedFuture(null)

        when:
            def result = locationService.deleteLocation(loc.getName(), 1)

        then:
            def saveResult = result?.get()
            saveResult == null
            1 * locationDao.deleteLocation(loc.getName(), 1)
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
