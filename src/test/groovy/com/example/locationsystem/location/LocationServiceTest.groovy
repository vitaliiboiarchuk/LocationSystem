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
            def result = locationService.saveLocation(loc,1L)

        then:
            def saveResult = result?.get()
            saveResult == null
            1 * locationDao.saveLocation(loc)
    }

    def "findLocationByNameAndUserId should return location"() {

        given:
            locationDao.findLocationByNameAndUserId(loc.getName(), 1) >> CompletableFuture.completedFuture(loc)

        when:
            def result = locationService.findLocationByNameAndUserId(loc.getName(), 1)

        then:
            def location = result.get()
            location == loc
    }

    def "findAllUserLocations should return locations"() {

        given:
            locationDao.findAllUserLocations(1) >> CompletableFuture.completedFuture(locs)

        when:
            def result = locationService.findAllUserLocations(1)

        then:
            def locsList = result.get()
            locsList == locs
    }

    def "should delete location"() {

        given:
            locationDao.deleteLocation(loc.getId(), 1) >> CompletableFuture.completedFuture(null)

        when:
            def result = locationService.deleteLocation(loc.getId(), 1)

        then:
            def saveResult = result?.get()
            saveResult == null
            1 * locationDao.deleteLocation(loc.getId(), 1)
    }

    def "should find not shared locations to user"() {

        given:
            locationDao.findNotSharedToUserLocations(1, 2) >> CompletableFuture.completedFuture(locs)

        when:
            def result = locationService.findNotSharedToUserLocations(1, 2)

        then:
            def locsList = result.get()
            locsList == locs
    }

    def "should find location by id"() {

        given:
            locationDao.findLocationById(loc.getId()) >> CompletableFuture.completedFuture(loc)

        when:
            def result = locationService.findLocationById(loc.getId())

        then:
            def location = result.get()
            loc == location
    }

//    def "should find location by name"() {
//
//        given:
//            locationDao.findLocationByName(loc.getName()) >> CompletableFuture.completedFuture(loc)
//
//        when:
//            def result = locationService.findLocationByName(loc.getName())
//
//        then:
//            def location = result.get()
//            loc == location
//    }

}
