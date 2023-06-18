package com.example.locationsystem.user

import spock.lang.Specification

import java.util.concurrent.CompletableFuture

class UserServiceTest extends Specification {

    UserDao userDao

    UserService userService

    def expectedUser = new User(1L,"user1","name1","pass1")
    def expectedUser2 = new User(2L,"user2","name2","pass2")
    def userList = new ArrayList()


    def setup() {
        userDao = Mock(UserDao)
        userService = new UserServiceImpl(userDao)

        userList << expectedUser
    }

    def "findByUsername should return User"() {
        given:
        userDao.findByUsername(expectedUser.username) >> CompletableFuture.completedFuture(expectedUser)

        when:
        def result = userService.findByUsername(expectedUser.username)

        then:
        def user = result.get()
        user == expectedUser
    }

    def "findByUsernameAndPassword should return User"() {
        given:
        userDao.findUserByUsernameAndPassword(expectedUser.username,expectedUser.password) >> CompletableFuture.completedFuture(expectedUser)

        when:
        def result = userService.findUserByUsernameAndPassword(expectedUser.username,expectedUser.password)

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
        userDao.findById(expectedUser.id) >> CompletableFuture.completedFuture(expectedUser)

        when:
        def result = userService.findById(expectedUser.id)

        then:
        def user = result.get()
        user == expectedUser
    }

    def "findUsersToShare should return Users"() {
        given:
        userDao.findUsersToShare(expectedUser2.id) >> CompletableFuture.completedFuture(userList)

        when:
        def result = userService.findUsersToShare(expectedUser2.id)

        then:
        def users = result.get()
        users == userList
    }

    def "findAllUsersWithAccessOnLocation should return Users"() {
        given:
        userDao.findAllUsersWithAccessOnLocation(1L,"ADMIN",expectedUser2.id) >> CompletableFuture.completedFuture(userList)

        when:
        def result = userService.findAllUsersWithAccessOnLocation(1L,"ADMIN",expectedUser2.id)

        then:
        def users = result.get()
        users == userList
    }

    def "should return null if owner is found and Id's match"() {
        given:
        Long locationId = 1L
        Long id = 10L
        User owner = new User(id,"username10","name10","pass10")
        userDao.findLocationOwner(1L) >> CompletableFuture.completedFuture(owner)

        when:
        def result = userService.findLocationOwner(locationId,id)

        then:
        result.get() == null
    }

    def "should return owner if owner is found and Id's don't match"() {
        given:
        Long locationId = 1L
        Long id = 10L
        Long ownerId = 11L
        User owner = new User(ownerId,"username10","name10","pass10")
        userDao.findLocationOwner(1L) >> CompletableFuture.completedFuture(owner)

        when:
        def result = userService.findLocationOwner(locationId,id)

        then:
        result.get() == owner
    }

}
