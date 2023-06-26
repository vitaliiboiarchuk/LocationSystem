package com.example.locationsystem.userAccess

import com.example.locationsystem.location.Location
import com.example.locationsystem.user.User
import spock.lang.Specification
import spock.lang.Subject

import java.util.concurrent.CompletableFuture

class UserAccessServiceTest extends Specification {

    UserAccessDao userAccessDao

    @Subject
    UserAccessService userAccessService

    User user
    User user2
    Location loc
    UserAccess userAccess

    def setup() {

        userAccessDao = Mock(UserAccessDao)
        userAccessService = new UserAccessServiceImpl(userAccessDao)

        user = new User(1L, "user1", "user1", "pass1")
        user2 = new User(2L, "user2", "user2", "pass2")
        loc = new Location(1L, "name1", "add1", user)
        userAccess = new UserAccess(1L, "ADMIN", user2, loc)
    }

    def "should insert user access into database"() {

        given:
            def userAccess = new UserAccess(1L, "title1", user2, loc)
            userAccessDao.saveUserAccess(userAccess) >> CompletableFuture.completedFuture(null)

        when:
            def result = userAccessService.saveUserAccess(userAccess)

        then:
            def saveResult = result?.get()
            saveResult == null

            1 * userAccessDao.saveUserAccess(userAccess)
    }

    def "should find user access"() {

        given:
            userAccessDao.findUserAccess(loc.getId(), user2.getId()) >> CompletableFuture.completedFuture(userAccess)

        when:
            def result = userAccessService.findUserAccess(loc.getId(), user2.getId())

        then:
            UserAccess access = result.get()
            access.getTitle() == 'ADMIN'
    }

    def "should change user access"() {

        given:
            userAccessDao.findUserAccess(loc.getId(), user2.getId()) >> CompletableFuture.completedFuture(userAccess)
            userAccessDao.changeUserAccess(userAccess.getTitle(), loc.getId(), user2.getId()) >> CompletableFuture.completedFuture(null)

        when:
            def result = userAccessService.changeUserAccess(loc.getId(), user2.getId())

        then:
            def saveResult = result?.get()
            saveResult == null

            1 * userAccessDao.changeUserAccess("READ", loc.getId(), user2.getId())
    }
}
