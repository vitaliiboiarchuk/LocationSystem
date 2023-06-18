package com.example.locationsystem.userAccess

import com.example.locationsystem.location.Location
import com.example.locationsystem.user.User
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

class UserAccessServiceTest extends Specification {

    UserAccessDao userAccessDao

    UserAccessService userAccessService

    def setup() {
        userAccessDao = Mock(UserAccessDao)
        userAccessService = new UserAccessServiceImpl(userAccessDao)
    }

    def "should insert user access into database"() {
        given:
        def user = new User(1L,"user1","user1","pass1")
        def user2 = new User(2L,"user2","user2","pass2")
        def loc = new Location(1L,"name1","add1",user)
        def userAccess = new UserAccess(1L,"title1",user2,loc)
        userAccessDao.saveUserAccess(userAccess) >> CompletableFuture.completedFuture(null)

        when:
        def result = userAccessService.saveUserAccess(userAccess)

        then:
        def saveResult = result?.get()
        saveResult == null

        1 * userAccessDao.saveUserAccess(userAccess)
    }

    def "should change user access"() {
        given:
        def user = new User(1L,"user1","user1","pass1")
        def user2 = new User(2L,"user2","user2","pass2")
        def loc = new Location(1L,"name1","add1",user)
        userAccessDao.changeUserAccess(loc.id, user2.id) >> CompletableFuture.completedFuture(null)

        when:
        def result = userAccessService.changeUserAccess(loc.id,user2.id)

        then:
        def saveResult = result?.get()
        saveResult == null

        1 * userAccessDao.changeUserAccess(loc.id, user2.id)
    }
}
