package com.example.locationsystem.user

import com.example.locationsystem.location.Location
import com.example.locationsystem.userAccess.UserAccess
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import spock.lang.Specification

@DataJpaTest
class UserRepositoryTest extends Specification {

    @Autowired
    TestEntityManager testEntityManager

    @Autowired
    UserRepository userRepository

    def user = new User("test@gmail.com","Test","1234",1)
    def locOwner = new User("loc@gmail.com","Test","1234",1)
    def location = new Location("Test","Test",locOwner)
    def access = new UserAccess("READ",user,location)

    def users = new ArrayList<>()


    void setup() {
        testEntityManager.persist(user)
        testEntityManager.persist(locOwner)
        testEntityManager.persist(location)
        testEntityManager.persist(access)

        users << user
    }

    def "should find user by username"() {
        when:
        def result = userRepository.findByUsername("test@gmail.com")

        then:
        user == result
    }

    def "should find all users by id not like specified"() {
        when:
        def result = userRepository.findAllByIdNotLike(locOwner.id)

        then:
        users == result
    }

    def "should find all users with title access on location"() {
        when:
        def result = userRepository.findAllUsersWithAccessOnLocation(location.id,"READ")

        then:
        users == result
    }

    def "should find location owner"() {
        when:
        def result = userRepository.findUserByLocationId(location.id)

        then:
        locOwner == result
    }

}

