package com.example.locationsystem.user

import spock.lang.Specification
import spock.lang.Subject

import java.util.concurrent.CompletableFuture

class UserServiceTest extends Specification {

    UserDao userDao

    @Subject
    UserService userService

    User expectedUser

    User expectedUser2

    List<User> userList

    def setup() {

        userDao = Mock(UserDao)
        userService = new UserServiceImpl(userDao)

        expectedUser = new User(1L, "user1", "name1", "pass1")
        expectedUser2 = new User(2L, "user2", "name2", "pass2")

        userList = new ArrayList()

        userList << expectedUser
    }

    def "findByUsername should return User"() {

        given:
            userDao.findByUsername(expectedUser.getUsername()) >> CompletableFuture.completedFuture(expectedUser)

        when:
            def result = userService.findByUsername(expectedUser.getUsername())

        then:
            def user = result.get()
            user == expectedUser
    }

    def "findByUsernameAndPassword should return User"() {

        given:
            userDao.findUserByUsernameAndPassword(expectedUser.getUsername(), expectedUser.getPassword()) >> CompletableFuture.completedFuture(expectedUser)

        when:
            def result = userService.findUserByUsernameAndPassword(expectedUser.getUsername(), expectedUser.getPassword())

        then:
            def user = result.get()
            user == expectedUser
    }

    def "saveUser should insert User into database"() {

        given:
            userDao.saveUser(expectedUser) >> CompletableFuture.completedFuture(null)

        when:
            def result = userService.saveUser(expectedUser)

        then:
            def saveResult = result?.get()
            saveResult == null
            1 * userDao.saveUser(expectedUser)
    }

    def "findById should return User"() {

        given:
            userDao.findById(expectedUser.getId()) >> CompletableFuture.completedFuture(expectedUser)

        when:
            def result = userService.findById(expectedUser.getId())

        then:
            def user = result.get()
            user == expectedUser
    }

    def "findAllUsersWithAccessOnLocation should return Users"() {

        given:
            userDao.findAllUsersWithAccessOnLocation(1L, "ADMIN", expectedUser2.getId()) >> CompletableFuture.completedFuture(userList)

        when:
            def result = userService.findAllUsersWithAccessOnLocation(1L, "ADMIN", expectedUser2.getId())

        then:
            def users = result.get()
            users == userList
    }

    def "should return null if owner is found and Id's match"() {

        given:
            User owner = new User(10L, "username10", "name10", "pass10")
            userDao.findLocationOwner(1L) >> CompletableFuture.completedFuture(owner)

        when:
            def result = userService.findLocationOwner(1L, owner.getId())

        then:
            result.get() == null
    }

    def "should return owner if owner is found and Id's not match"() {

        given:
            User owner = new User(11L, "username10", "name10", "pass10")
            userDao.findLocationOwner(1L) >> CompletableFuture.completedFuture(owner)

        when:
            def result = userService.findLocationOwner(1L, 10L)

        then:
            result.get() == owner
    }
}
