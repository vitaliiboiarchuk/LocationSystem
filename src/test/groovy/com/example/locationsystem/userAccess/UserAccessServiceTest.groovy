package com.example.locationsystem.userAccess

import spock.lang.Specification
import spock.lang.Subject

import java.util.concurrent.CompletableFuture

class UserAccessServiceTest extends Specification {

    UserAccessDao userAccessDao

    @Subject
    UserAccessService userAccessService

    UserAccess userAccess

    def setup() {

        userAccessDao = Mock(UserAccessDao)
        userAccessService = new UserAccessServiceImpl(userAccessDao)

        userAccess = new UserAccess("ADMIN", 1, 2)
    }

    def "should insert user access into database"() {

        given:
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
            userAccessDao.findUserAccess(userAccess, 2) >> CompletableFuture.completedFuture(userAccess)

        when:
            def result = userAccessService.findUserAccess(userAccess, 2)

        then:
            UserAccess access = result.get()
            access.getTitle() == 'ADMIN'
            access.getUserId() == 1
            access.getLocationId() == 2
    }

    def "should change user access"() {

        given:
            userAccessDao.changeUserAccess(userAccess) >> CompletableFuture.completedFuture(null)

        when:
            def result = userAccessService.changeUserAccess(userAccess)

        then:
            def saveResult = result?.get()
            saveResult == null

            1 * userAccessDao.changeUserAccess(userAccess)
    }
}
