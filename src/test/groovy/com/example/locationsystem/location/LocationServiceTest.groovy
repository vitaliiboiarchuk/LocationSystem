package com.example.locationsystem.location

import com.example.locationsystem.user.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest
class LocationServiceTest extends Specification {

    @Autowired
    LocationService locationService

    @Autowired
    LocationRepository locationRepository

    def user1 = new User("test@gmail.com", "test", "1234", 1)
    def location1 = new Location("test", "test", user1)
    def location4 = new Location("test3", "test", user1)

    def addedLocationsUser1 = new ArrayList<>()
    def accessLocationsUser1 = new ArrayList<>()

    def user2 = new User("test2@gmail.com","tet","1234",1)
    def location2 = new Location("test2", "test", user2)

    def addedLocationsUser2 = new ArrayList<>()
    def accessLocationsUser2 = new ArrayList<>()


    def user3 = new User("test3@gmail.com","tet","1234",1)
    def location3 = new Location("test3", "test", user3)


    void setup() {
        locationRepository = Mock()
        locationService = new LocationServiceImpl(locationRepository)

        user1.setId(1L)
        location1.setId(1L)

        addedLocationsUser1 << location1
        addedLocationsUser1 << location4
        accessLocationsUser1 << location2
        accessLocationsUser1 << location3

        user2.setId(2L)

        addedLocationsUser2 << location2
        accessLocationsUser2 << location1
    }

    def "should save location"() {
        when:
        locationService.saveLocation(location1)

        then:
        1 * locationRepository.save(location1)
    }

    def "should find location by name"() {
        given:
        locationRepository.findLocationByName(location1.name) >> location1

        when:
        def result = locationService.findLocationByName(location1.name)

        then:
        location1 == result
    }

    def "should find all added locations"() {
        given:
        locationRepository.findLocationsByUserId(user1.id) >> addedLocationsUser1

        when:
        def result = locationService.findAllAddedLocations(user1.id)

        then:
        addedLocationsUser1 == result
    }

    def "should find all locations with access"() {
        given:
        locationRepository.findAllAccessLocationsByUserIdAndTitle(user2.id,"ADMIN") >> accessLocationsUser2

        when:
        def result = locationService.findAllLocationsWithAccess(user2.id,"ADMIN")

        then:
        accessLocationsUser2 == result
    }

    def "should delete location"() {
        when:
        locationService.deleteLocation(location1.id)

        then:
        1 * locationRepository.deleteById(location1.id)
    }

    def "should find not shared to user locations"() {
        given:
        def locsToShare = new ArrayList()
        locsToShare << location4
        locsToShare << location3

        locationRepository.findLocationsByUserId(user1.id) >> addedLocationsUser1
        locationRepository.findAllAccessLocationsByUserIdAndTitle(user1.id,"ADMIN") >> accessLocationsUser1

        locationRepository.findLocationsByUserId(user2.id) >> addedLocationsUser2
        locationRepository.findAllAccessLocationsByUserIdAndTitle(user2.id,"ADMIN") >> accessLocationsUser2
        locationRepository.findAllAccessLocationsByUserIdAndTitle(user2.id,"READ") >> accessLocationsUser2

        when:
        def result = locationService.findNotSharedToUserLocations(user1.id,user2.id)

        then:
        locsToShare == result
    }

}

