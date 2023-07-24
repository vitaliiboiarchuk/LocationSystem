package com.example.locationsystem.user

import com.example.locationsystem.event.EventService
import com.example.locationsystem.utils.EmailUtils
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

class UserServiceTest extends Specification {

    @Shared
    def user = new User(id: 100L, username: "user1@gmail.com", name: "name1", password: "pass1")

    UserDao userDao
    UserService userService
    EmailUtils emailUtils
    EventService eventService

    def setup() {

        userDao = Mock(UserDao)
        emailUtils = Mock(EmailUtils)
        eventService = Mock(EventService)
        userService = new UserServiceImpl(userDao, emailUtils, eventService)
    }

    def "saveUser should insert user into database"() {

        given:
            def userToSave = Stub(User)

        and:
            def expectedUser = Stub(User)

        when:
            def result = userService.saveUser(userToSave).join()

        then:
            result == expectedUser

        then:
            1 * userDao.saveUser(userToSave) >> CompletableFuture.completedFuture(expectedUser)
            1 * eventService.publishUserCreatedEvent(expectedUser) >> null
    }

    def "findByEmail should return User"() {

        given:
            userDao.findUserByEmail(user.getUsername()) >> CompletableFuture.completedFuture(Optional.of(user))

        when:
            def result = userService.findUserByEmail(user.getUsername()).join()

        then:
            result.isPresent()
            result.get().getUsername() == user.getUsername()
    }

    def "findByEmailAndPassword should return User"() {

        given:
            userDao.findUserByEmailAndPassword(user.getUsername(), user.getPassword()) >> CompletableFuture.completedFuture(user)

        when:
            def result = userService.findUserByEmailAndPassword(user.getUsername(), user.getPassword())

        then:
            result.get() == user
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

    def "deleteUserByEmail should delete user and publish an event if the user exists"() {

        when:
            userService.deleteUserByEmail("user1@gmail.com")

        then:
            1 * userDao.findUserByEmail(user.getUsername()) >> CompletableFuture.completedFuture(Optional.of(user))
            1 * userDao.deleteUserByEmail(user.getUsername()) >> CompletableFuture.completedFuture(null)
            1 * eventService.publishUserDeletedEvent(user) >> null
    }

    def "findById should return User"() {

        given:
            userDao.findUserById(user.getId()) >> CompletableFuture.completedFuture(user)

        when:
            def result = userService.findUserById(user.getId())

        then:
            result.get() == user
    }
}
