package com.example.locationsystem.userAccess

import com.example.locationsystem.location.Location
import com.example.locationsystem.user.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest
class UserAccessServiceTest extends Specification {

    @Autowired
    UserAccessService userAccessService

    @Autowired
    UserAccessRepository userAccessRepository

    def locOwner = new User("test@gmail.com", "test", "1234", 1)
    def user1 = new User("test1@gmail.com", "test", "1234", 1)
    def location = new Location("test", "test", locOwner)
    def userAccess = new UserAccess("ADMIN",user1,location)

    void setup() {
        userAccessRepository = Mock()
        userAccessService = new UserAccessServiceImpl(userAccessRepository)
    }

    def "should save user access"() {
        given:
        userAccess.setId(1L)

        when:
        userAccessService.saveUserAccess(userAccess)

        then:
        1 * userAccessRepository.save(userAccess)
    }

    def "should change user access"() {
        given:
        user1.setId(1L)
        userAccessRepository.findUserAccessByUserId(user1.id) >> userAccess

        when:
        userAccessService.changeUserAccess(user1.id)

        then:
        userAccess.title == "READ"
        1 * userAccessRepository.save(userAccess)
    }
}

