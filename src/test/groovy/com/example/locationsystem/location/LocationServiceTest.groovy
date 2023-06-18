package com.example.locationsystem.location

import com.example.locationsystem.user.User
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

class LocationServiceTest extends Specification {

    LocationDao locationDao

    LocationService locationService

    User user

    Location loc

    List<Location> locs

    def setup() {
        locationDao = Mock(LocationDao)
        locationService = new LocationServiceImpl(locationDao)

        user = new User(1L,"user1","user1","pass1")
        loc = new Location(1L,"name1","add1",user)

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
        locationDao.findLocationByNameAndUserId(loc.name,user.id) >> CompletableFuture.completedFuture(loc)

        when:
        def result = locationService.findLocationByNameAndUserId(loc.name,user.id)

        then:
        def location = result.get()
        location == loc
    }

    def "findAllAddedLocations should return locations"() {
        given:
        locationDao.findAllAddedLocations(user.id) >> CompletableFuture.completedFuture(locs)

        when:
        def result = locationService.findAllAddedLocations(user.id)

        then:
        def locsList = result.get()
        locsList == locs
    }

    def "findAllLocationsWithAccess should return locations"() {
        given:
        def user2 = new User(2L,"user2","user2","pass2")
        locationDao.findAllLocationsWithAccess(user2.id,"ADMIN") >> CompletableFuture.completedFuture(locs)

        when:
        def result = locationDao.findAllLocationsWithAccess(user2.id,"ADMIN")

        then:
        def locsList = result.get()
        locsList == locs
    }

    def "should delete location"() {
        given:
        locationDao.deleteLocation(loc.id,user.id) >> CompletableFuture.completedFuture(null)

        when:
        def result = locationService.deleteLocation(loc.id,user.id)

        then:
        def saveResult = result?.get()
        saveResult == null
        1 * locationDao.deleteLocation(loc.id,user.id)
    }

    def "should find not shared locations to user"() {
        given:
        def user1 = new User(15L, "test@gmail.com", "test", "1234")
        def user2 = new User(16L, "test2@gmail.com", "test", "1234")
        def user3 = new User(17L, "test3@gmail.com", "test", "1234")

        def loc1 = new Location(15L, "test", "test", user1)
        def loc2 = new Location(16L, "test", "test", user2)
        def loc3 = new Location(17L, "test", "test", user3)
        def loc4 = new Location(18L, "test", "test", user1)

        def addedLocsUser1 = [loc1,loc4]
        def accessLocsUser1 = [loc2,loc3]
        def addedLocsUser2 = [loc2]
        def accessLocsUser2 = [loc1]

        def locsToShare = [loc4,loc3]

        def addedLocsUser1Future = CompletableFuture.completedFuture(addedLocsUser1)
        def addedLocsUser2Future = CompletableFuture.completedFuture(addedLocsUser2)
        def accessLocsUser1Future = CompletableFuture.completedFuture(accessLocsUser1)
        def accessLocsUser2Future = CompletableFuture.completedFuture(accessLocsUser2)

        locationDao.findAllAddedLocations(user1.id) >> addedLocsUser1Future
        locationDao.findAllLocationsWithAccess(user1.id,"ADMIN") >> accessLocsUser1Future

        locationDao.findAllAddedLocations(user2.id) >> addedLocsUser2Future
        locationDao.findAllLocationsWithAccess(user2.id,"ADMIN") >> accessLocsUser2Future
        locationDao.findAllLocationsWithAccess(user2.id,"READ") >> accessLocsUser2Future

        when:
        def result = locationService.findNotSharedToUserLocations(user1.id,user2.id)

        then:
        def locations = result.get()
        locations == locsToShare
    }

}
