package com.example.locationsystem.location

import com.example.locationsystem.user.User
import spock.lang.Specification
import spock.lang.Subject

import java.util.concurrent.CompletableFuture

class LocationServiceTest extends Specification {

    LocationDao locationDao

    @Subject
    LocationService locationService

    User user

    Location loc

    List<Location> locs

    def setup() {

        locationDao = Mock(LocationDao)
        locationService = new LocationServiceImpl(locationDao)

        user = new User(1L, "user1", "user1", "pass1")
        loc = new Location(1L, "name1", "add1", user)

        locs = new ArrayList()
        locs << loc
    }

    def "saveLocation should insert location into database"() {

        given:
            locationDao.saveLocation(loc) >> CompletableFuture.completedFuture(null)

        when:
            def result = locationService.saveLocation(loc)

        then:
            def saveResult = result?.get()
            saveResult == null
            1 * locationDao.saveLocation(loc)
    }

    def "findLocationByNameAndUserId should return location"() {

        given:
            locationDao.findLocationByNameAndUserId(loc.getName(), user.getId()) >> CompletableFuture.completedFuture(loc)

        when:
            def result = locationService.findLocationByNameAndUserId(loc.getName(), user.getId())

        then:
            def location = result.get()
            location == loc
    }

    def "findAllAddedLocations should return locations"() {

        given:
            locationDao.findAllAddedLocations(user.getId()) >> CompletableFuture.completedFuture(locs)

        when:
            def result = locationService.findAllAddedLocations(user.getId())

        then:
            def locsList = result.get()
            locsList == locs
    }

    def "findAllLocationsWithAccess should return locations"() {

        given:
            def user2 = new User(2L, "user2", "user2", "pass2")
            locationDao.findAllLocationsWithAccess(user2.getId(), "ADMIN") >> CompletableFuture.completedFuture(locs)

        when:
            def result = locationDao.findAllLocationsWithAccess(user2.getId(), "ADMIN")

        then:
            def locsList = result.get()
            locsList == locs
    }

    def "should delete location"() {

        given:
            locationDao.deleteLocation(loc.getId(), user.getId()) >> CompletableFuture.completedFuture(null)

        when:
            def result = locationService.deleteLocation(loc.getId(), user.getId())

        then:
            def saveResult = result?.get()
            saveResult == null
            1 * locationDao.deleteLocation(loc.getId(), user.getId())
    }

    def "should find not shared locations to user"() {

        given:
            def user2 = new User(2L, "user2", "user2", "pass2")
            locationDao.findNotSharedToUserLocations(user.getId(), user2.getId()) >> CompletableFuture.completedFuture(locs)

        when:
            def result = locationService.findNotSharedToUserLocations(user.getId(), user2.getId())

        then:
            def locsList = result.get()
            locsList == locs
    }

    def "should find location by id"() {

        given:
            locationDao.findById(loc.getId()) >> CompletableFuture.completedFuture(loc)

        when:
            def result = locationService.findById(loc.getId())

        then:
            def location = result.get()
            loc == location
    }

}
