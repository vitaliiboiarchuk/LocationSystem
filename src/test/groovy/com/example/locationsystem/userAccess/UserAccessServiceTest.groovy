package com.example.locationsystem.userAccess

import org.springframework.context.ApplicationEventPublisher
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

class UserAccessServiceTest extends Specification {

    @Shared
    def userAccess = new UserAccess(id: 100L, title: "ADMIN", userId: 1L, locationId: 2L)

    UserAccessDao userAccessDao
    UserAccessService userAccessService
    ApplicationEventPublisher eventPublisher

    def setup() {

        userAccessDao = Mock(UserAccessDao)
        eventPublisher = Mock(ApplicationEventPublisher)
        userAccessService = new UserAccessServiceImpl(userAccessDao, eventPublisher)
    }

    def "should insert user access into database"() {

        given:
            def userAccessToSave = Stub(UserAccess)

        and:
            def expectedUserAccess = Stub(UserAccess)

        when:
            def result = userAccessService.saveUserAccess(userAccessToSave).join()

        then:
            result == expectedUserAccess

        then:
            1 * userAccessDao.saveUserAccess(userAccessToSave) >> CompletableFuture.completedFuture(expectedUserAccess)
            1 * eventPublisher.publishEvent(_) >> null
    }

    def "should find user access"() {

        given:
            userAccessDao.findUserAccess(userAccess, 1L) >> CompletableFuture.completedFuture(userAccess)

        when:
            def result = userAccessService.findUserAccess(userAccess, 1L).join()

        then:
            result == userAccess
    }

    def "should change user access"() {

        when:
            userAccessService.changeUserAccess(userAccess)

        then:
            1 * userAccessDao.changeUserAccess(userAccess) >> CompletableFuture.completedFuture(userAccess)
    }
}
