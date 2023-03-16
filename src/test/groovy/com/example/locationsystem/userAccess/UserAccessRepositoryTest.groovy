package com.example.locationsystem.userAccess

import com.example.locationsystem.location.Location
import com.example.locationsystem.user.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import spock.lang.Specification

@DataJpaTest
class UserAccessRepositoryTest extends Specification {

    @Autowired
    TestEntityManager testEntityManager

    @Autowired
    UserAccessRepository userAccessRepository

    def locOwner = new User("tet@gmail.com","test","1234",1)
    def location = new Location("test@gmail.com","test",locOwner)
    def user = new User("user@gmail.com","user","1234",1)
    def access = new UserAccess("ADMIN",user,location)


    void setup() {
        testEntityManager.persist(locOwner)
        testEntityManager.persist(location)
        testEntityManager.persist(user)
        testEntityManager.persist(access)
    }

    def "should find user access by location id and user id"() {
        when:
        def result = userAccessRepository.findUserAccessByLocationIdAndUserId(location.id, user.id)

        then:
        access == result
    }

}

