package com.example.locationsystem.user

import spock.lang.Specification
import spock.lang.Subject

import java.util.concurrent.CompletableFuture

class UserServiceTest extends Specification {

    UserDao userDao

    @Subject
    UserService userService

    User user

    def setup() {

        userDao = Mock(UserDao)
        userService = new UserServiceImpl(userDao)

        user = new User("user1", "name1", "pass1")
        user.setId(100L)
    }

    def "findByUsername should return User"() {

        given:
            userDao.findByUsername(user.getUsername()) >> CompletableFuture.completedFuture(user)

        when:
            def result = userService.findByUsername(user.getUsername())

        then:
            def user = result.get()
            user == user
    }

    def "findByUsernameAndPassword should return User"() {

        given:
            userDao.findUserByUsernameAndPassword(user.getUsername(), user.getPassword()) >> CompletableFuture.completedFuture(user)

        when:
            def result = userService.findUserByUsernameAndPassword(user.getUsername(), user.getPassword())

        then:
            def user = result.get()
            user == user
    }

    def "saveUser should insert User into database"() {

        given:
            userDao.saveUser(user) >> CompletableFuture.completedFuture(null)

        when:
            def result = userService.saveUser(user)

        then:
            def saveResult = result?.get()
            saveResult == null
            1 * userDao.saveUser(user)
    }

    def "findById should return User"() {

        given:
            userDao.findById(user.getId()) >> CompletableFuture.completedFuture(user)

        when:
            def result = userService.findById(user.getId())

        then:
            def user = result.get()
            user == user
    }

    def "findAllUsersWithAccessOnLocation should return Users"() {

        given:
            def adminAccessUser = new User("name", "name", "pass")
            def readAccessUser = new User("name", "name", "pass")
            def adminAccessUsers = [adminAccessUser]
            def readAccessUsers = [readAccessUser]
            def allAccessUsers = adminAccessUsers + readAccessUsers
            userDao.findAllUsersWithAccessOnLocation(1L, "ADMIN", user.getId()) >> CompletableFuture.completedFuture(adminAccessUsers)
            userDao.findAllUsersWithAccessOnLocation(1L, "READ", user.getId()) >> CompletableFuture.completedFuture(readAccessUsers)

        when:
            def result = userService.findAllUsersWithAccessOnLocation(1L, user.getId())

        then:
            def users = result.get()
            users == allAccessUsers
    }

    def "should return null if owner is found and ID's not match"() {

        given:
            User otherOwner = new User("username11", "name11", "pass11")
            otherOwner.setId(101L)
            userDao.findLocationOwner(1L) >> CompletableFuture.completedFuture(otherOwner)

        when:
            def result = userService.findLocationOwner(1L, user.getId())

        then:
            result.get() == null
    }

    def "should return null if owner is not found"() {

        given:
            userDao.findLocationOwner(1L) >> CompletableFuture.completedFuture(null)

        when:
            def result = userService.findLocationOwner(1L, user.getId())

        then:
            result.get() == null
    }

    def "should return owner if owner is found"() {

        given:
            userDao.findLocationOwner(1L) >> CompletableFuture.completedFuture(user)

        when:
            def result = userService.findLocationOwner(1L, user.getId())

        then:
            result.get() == user
    }

    def "should delete user"() {

        given:
            userDao.deleteUserByUsername("test@gmail.com") >> CompletableFuture.completedFuture(null)

        when:
            def result = userService.deleteUserByUsername("test@gmail.com")

        then:
            def saveResult = result?.get()
            saveResult == null
            1 * userDao.deleteUserByUsername("test@gmail.com")
    }
}
