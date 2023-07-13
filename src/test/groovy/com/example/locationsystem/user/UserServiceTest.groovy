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
        user.setId(1L)
    }

    def "findByEmail should return User"() {

        given:
            userDao.findUserByEmail(user.getUsername()) >> CompletableFuture.completedFuture(user)

        when:
            def result = userService.findUserByEmail(user.getUsername())

        then:
            def user = result.get()
            user == this.user
    }

    def "findByEmailAndPassword should return User"() {

        given:
            userDao.findUserByEmailAndPassword(user.getUsername(), user.getPassword()) >> CompletableFuture.completedFuture(user)

        when:
            def result = userService.findUserByEmailAndPassword(user.getUsername(), user.getPassword())

        then:
            def user = result.get()
            user == this.user
    }

    def "saveUser should insert User into database"() {

        given:
            userDao.saveUser(user) >> CompletableFuture.completedFuture(null)

        when:
            def result = userService.saveUser(user)

        then:
            def savedUser = result?.get()
            savedUser == null
            1 * userDao.saveUser(user)
    }

    def "findById should return User"() {

        given:
            userDao.findUserById(user.getId()) >> CompletableFuture.completedFuture(user)

        when:
            def result = userService.findUserById(user.getId())

        then:
            def user = result.get()
            user == this.user
    }

    def "findAllUsersWithAccessOnLocation should return Users"() {

        given:
            def accessUser = new User("name", "name", "pass")
            def accessUsers = [accessUser]
            userDao.findAllUsersOnLocation(1L, user.getId()) >> CompletableFuture.completedFuture(accessUsers)

        when:
            def result = userService.findAllUsersOnLocation(1L, user.getId())

        then:
            def users = result.get()
            users == accessUsers
    }

    def "should return owner if owner is found"() {

        given:
            userDao.findLocationOwner(1L) >> CompletableFuture.completedFuture(user)

        when:
            def result = userService.findLocationOwner(1L)

        then:
            result.get() == user
    }

    def "should delete user"() {

        given:
            userDao.deleteUserByEmail("test@gmail.com") >> CompletableFuture.completedFuture(null)

        when:
            def result = userService.deleteUserByEmail("test@gmail.com")

        then:
            def saveResult = result?.get()
            saveResult == null
            1 * userDao.deleteUserByEmail("test@gmail.com")
    }
}
