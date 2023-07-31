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

    def "should change user access"() {

        given:
            userAccessDao.changeUserAccess(userAccess, 1) >> CompletableFuture.completedFuture(userAccess)

        when:
            def result = userAccessService.changeUserAccess(userAccess, 1)

        then:
            UserAccess access = result.get()
            access.getTitle() == 'ADMIN'
            access.getUserId() == 1L
            access.getLocationId() == 2L
    }
}
