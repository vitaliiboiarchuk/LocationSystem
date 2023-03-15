package com.example.locationsystem.location

import com.example.locationsystem.user.User
import com.example.locationsystem.userAccess.UserAccess
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import spock.lang.Specification

@DataJpaTest
class LocationRepositoryTest extends Specification {

    @Autowired
    TestEntityManager testEntityManager

    @Autowired
    LocationRepository locationRepository

    def locOwner = new User("test@gmail.com","Test","1234",1)
    def location = new Location("Test","Test",locOwner)
    def user = new User("test2@gmail.com","Test2","1234",1)
    def access = new UserAccess("ADMIN",user,location)

    def locations = new ArrayList<>()

    void setup() {
        testEntityManager.persist(locOwner)
        testEntityManager.persist(location)
        testEntityManager.persist(user)
        testEntityManager.persist(access)

        locations << location
    }

    def "should find location by name"() {
        when:
        def result = locationRepository.findLocationByName(location.name)

        then:
        location == result
    }

    def "should find location by user id"() {
        when:
        def result = locationRepository.findLocationsByUserId(locOwner.id)

        then:
        locations == result
    }

    def "should find locations by user id and title access"() {
        when:
        def result = locationRepository.findAllAccessLocationsByUserIdAndTitle(user.id,"ADMIN")

        then:
        locations == result
    }

}

