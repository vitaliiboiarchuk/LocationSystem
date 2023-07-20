package com.example.locationsystem.location

import com.example.locationsystem.user.User
import com.example.locationsystem.user.UserDao
import com.example.locationsystem.user.UserService
import com.example.locationsystem.userAccess.UserAccess
import com.example.locationsystem.userAccess.UserAccessDao
import com.example.locationsystem.userAccess.UserAccessService
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import javax.servlet.http.Cookie
import javax.sql.DataSource
import java.util.concurrent.CompletableFuture

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@AutoConfigureMockMvc
@SpringBootTest
class LocationControllerIntegrationTest extends Specification {

    @Autowired
    MockMvc mockMvc

    @Autowired
    LocationService locationService

    @Autowired
    LocationDao locationDao

    @Autowired
    UserService userService

    @Autowired
    UserDao userDao

    @Autowired
    UserAccessService userAccessService

    @Autowired
    UserAccessDao userAccessDao

    JdbcTemplate jdbcTemplate

    @Autowired
    DataSource dataSource

    void setup() {

        jdbcTemplate = new JdbcTemplate(dataSource)
    }

    private static final String DELETE_LOCATION_BY_NAME = "DELETE FROM locations WHERE name = 'name';"
    private static final String DELETE_LOCATION_BY_NAME_2 = "DELETE FROM locations WHERE name = 'name2';"
    private static final String DELETE_USER_BY_EMAIL = "DELETE FROM users WHERE username = 'test@gmail.com';"
    private static final String DELETE_USER_BY_EMAIL_2 = "DELETE FROM users WHERE username = 'test2@gmail.com';"

    def "should add location successfully"() {

        given:
            def user = new User("test@gmail.com", "test", "pass")
            CompletableFuture<User> savedUserFuture = userService.saveUser(user)
                .thenCompose({ result -> userService.findUserById(user.getId()) })

            def savedUser = savedUserFuture.join()
            def location = new Location("name", "address", savedUser.getId())

        when:
            def mvcResult = mockMvc.perform(post("/location/add")
                .cookie(new Cookie("user", savedUser.getId().toString()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(location)))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect { result ->
                    def jsonSlurper = new JsonSlurper()
                    def jsonResponse = jsonSlurper.parseText(mvcResult.response.contentAsString)
                    jsonResponse['name'] == location.getName()
                    jsonResponse['address'] == location.getAddress()
                }
        then:
            Optional<Location> addedLocationService = locationService.findLocationByNameAndUserId(location.getName(),
                savedUser.getId()).join()
            addedLocationService.get().getName() == location.getName()
            addedLocationService.get().getAddress() == location.getAddress()

        and:
            Optional<Location> addedLocationDao = locationDao.findLocationByNameAndUserId(location.getName(),
                savedUser.getId()).join()
            addedLocationDao.get().getName() == location.getName()
            addedLocationDao.get().getAddress() == location.getAddress()

        cleanup:
            jdbcTemplate.execute(DELETE_LOCATION_BY_NAME)
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL)
    }

    def "should throw MethodArgumentNotValidException when field is empty"() {

        given:
            def user = new User("test@gmail.com", "test", "pass")
            CompletableFuture<User> savedUserFuture = userService.saveUser(user)
                .thenCompose({ result -> userService.findUserById(user.getId()) })

            def savedUser = savedUserFuture.join()

            def emptyFieldLocation = new Location("", "add1", savedUser.getId())

        when:
            def result = mockMvc.perform(post("/location/add")
                .cookie(new Cookie("user", savedUser.getId().toString()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(emptyFieldLocation)))
                .andExpect(status().isBadRequest())
                .andReturn()

        then:
            def errorMessage = result.response.getHeader("errorMessage")
            errorMessage == "Field can not be empty"
            0 * locationService.saveLocation(emptyFieldLocation)
            0 * locationDao.saveLocation(emptyFieldLocation)

        cleanup:
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL)
    }

    def "should throw AlreadyExistsException when location already exists"() {

        given:
            def user = new User("test@gmail.com", "test", "pass")
            CompletableFuture<User> savedUserFuture = userService.saveUser(user)
                .thenCompose({ result -> userService.findUserById(user.getId()) })

            def savedUser = savedUserFuture.join()

            def location = new Location("name", "address", savedUser.getId())

            locationService.saveLocation(location, savedUser.getId())
                .thenCompose({ result2 -> locationService.findLocationById(location.getId()) })

        when:
            def result = mockMvc.perform(post("/location/add")
                .cookie(new Cookie("user", savedUser.getId().toString()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(location)))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("errorMessage", "Location with that name already exists"))

        then:
            0 * locationService.saveLocation(location)
            0 * locationDao.saveLocation(location)

        cleanup:
            jdbcTemplate.execute(DELETE_LOCATION_BY_NAME)
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL)
    }

    def "should show my locations"() {

        given:
            def user = new User("test@gmail.com", "test", "pass")
            CompletableFuture<User> savedUserFuture = userService.saveUser(user)
                .thenCompose({ result -> userService.findUserById(user.getId()) })

            def savedUser = savedUserFuture.join()

            def location = new Location("name", "address", savedUser.getId())

            CompletableFuture<Location> savedLocationFuture = locationService.saveLocation(location, savedUser.getId())
                .thenCompose({ result2 -> locationService.findLocationById(location.getId()) })

            def savedLocation = savedLocationFuture.join()

            def user2 = new User("test2@gmail.com", "test", "pass")
            CompletableFuture<User> savedUser2Future = userService.saveUser(user2)
                .thenCompose({ result3 -> userService.findUserById(user2.getId()) })

            def savedUser2 = savedUser2Future.join()

            def location2 = new Location("name2", "address", savedUser2.getId())

            CompletableFuture<Location> savedLocation2Future = locationService.saveLocation(location2, savedUser2.getId())
                .thenCompose({ result4 -> locationService.findLocationById(location2.getId()) })

            def savedLocation2 = savedLocation2Future.join()

            def userAccess = new UserAccess("ADMIN", savedUser.getId(), savedLocation2.getId())
            userAccessService.saveUserAccess(userAccess).join()

            def expectedLocs = [savedLocation, savedLocation2]

        expect:
            def mvcResult = mockMvc.perform(get("/location")
                .cookie(new Cookie("user", savedUser.getId().toString())))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect { result ->
                    def jsonSlurper = new JsonSlurper()
                    def jsonResponse = jsonSlurper.parseText(mvcResult.response.contentAsString)
                    expectedLocs.each { expectedLoc ->
                        jsonResponse.find { it['name'] == expectedLoc.name && it['address'] == expectedLoc.address }
                    }
                }

        cleanup:
            jdbcTemplate.execute(DELETE_LOCATION_BY_NAME)
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL)
            jdbcTemplate.execute(DELETE_LOCATION_BY_NAME_2)
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL_2)
    }

    def "should share location successfully"() {

        given:
            def user = new User("test@gmail.com", "test", "pass")
            CompletableFuture<User> savedUserFuture = userService.saveUser(user)
                .thenCompose({ result -> userService.findUserById(user.getId()) })

            def savedUser = savedUserFuture.join()

            def location = new Location("name", "address", savedUser.getId())

            CompletableFuture<Location> savedLocationFuture = locationService.saveLocation(location, savedUser.getId())
                .thenCompose({ result2 -> locationService.findLocationById(location.getId()) })

            def savedLocation = savedLocationFuture.join()

            def user2 = new User("test2@gmail.com", "test", "pass")
            CompletableFuture<User> savedUser2Future = userService.saveUser(user2)
                .thenCompose({ result3 -> userService.findUserById(user2.getId()) })

            def savedUser2 = savedUser2Future.join()

            def userAccess = new UserAccess("ADMIN", savedUser2.getId(), savedLocation.getId())

        when:
            def mvcResult = mockMvc.perform(post("/location/share")
                .cookie(new Cookie("user", savedUser.getId().toString()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userAccess)))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect { result ->
                    def jsonSlurper = new JsonSlurper()
                    def jsonResponse = jsonSlurper.parseText(mvcResult.response.contentAsString)
                    jsonResponse['title'] == userAccess.getTitle()
                    jsonResponse['locationId'] == userAccess.getLocationId()
                    jsonResponse['userId'] == userAccess.getUserId()
                }
        then:
            UserAccess addedUserAccessService = userAccessService.findUserAccess(userAccess, savedUser.getId()).join()
            userAccess.getTitle() == addedUserAccessService.getTitle()

        and:
            UserAccess addedUserAccessDao = userAccessDao.findUserAccess(userAccess, savedUser.getId()).join()
            userAccess.getTitle() == addedUserAccessDao.getTitle()

        cleanup:
            jdbcTemplate.execute(DELETE_LOCATION_BY_NAME)
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL)
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL_2)
    }

    def "should throw NoLocationOrUserFoundException when no location or user found to share"() {

        given:
            def user = new User("test@gmail.com", "test", "pass")
            CompletableFuture<User> savedUserFuture = userService.saveUser(user)
                .thenCompose({ result -> userService.findUserById(user.getId()) })

            def savedUser = savedUserFuture.join()

            def location = new Location("name", "address", savedUser.getId())

            CompletableFuture<Location> savedLocationFuture = locationService.saveLocation(location, savedUser.getId())
                .thenCompose({ result2 -> locationService.findLocationById(location.getId()) })

            def savedLocation = savedLocationFuture.join()

            def userAccess = new UserAccess("ADMIN", savedUser.getId() + 1, savedLocation.getId() + 1)

        when:
            def result = mockMvc.perform(post("/location/share")
                .cookie(new Cookie("user", savedUser.getId().toString()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userAccess)))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("errorMessage", "Location or user not found"))

        then:
            0 * userAccessService.saveUserAccess(userAccess)
            0 * userAccessDao.saveUserAccess(userAccess)

        cleanup:
            jdbcTemplate.execute(DELETE_LOCATION_BY_NAME)
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL)
    }

    def "should show friends on location if all my locations have location with specified id"() {

        given:
            def owner = new User("test@gmail.com", "test", "pass")
            CompletableFuture<User> savedUserFuture = userService.saveUser(owner)
                .thenCompose({ result -> userService.findUserById(owner.getId()) })

            def savedOwner = savedUserFuture.join()

            def location = new Location("name", "address", savedOwner.getId())

            CompletableFuture<Location> savedLocationFuture = locationService.saveLocation(location, savedOwner.getId())
                .thenCompose({ result2 -> locationService.findLocationById(location.getId()) })

            def savedLocation = savedLocationFuture.join()

            def friend = new User("test2@gmail.com", "test", "pass")
            CompletableFuture<User> savedUser2Future = userService.saveUser(friend)
                .thenCompose({ result3 -> userService.findUserById(friend.getId()) })

            def savedFriend = savedUser2Future.join()

            def userAccess = new UserAccess("ADMIN", savedFriend.getId(), savedLocation.getId())
            userAccessService.saveUserAccess(userAccess).join()

            def expectedFriends = [savedFriend]

        when:
            def mvcResult = mockMvc.perform(get("/location/{locationId}/", savedLocation.getId())
                .cookie(new Cookie("user", savedOwner.getId().toString())))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect { result ->
                    def jsonSlurper = new JsonSlurper()
                    def jsonResponse = jsonSlurper.parseText(mvcResult.response.contentAsString)
                    expectedFriends.each { expectedFriend ->
                        jsonResponse.find { it['name'] == expectedFriend.name && it['username'] == expectedFriend.username }
                    }
                }
        then:
            List<User> usersOnLocService = userService.findAllUsersOnLocation(savedLocation.getId(), savedOwner.getId()).join()
            usersOnLocService == expectedFriends

        and:
            List<User> usersOnLocDao = userDao.findAllUsersOnLocation(savedLocation.getId(), savedOwner.getId()).join()
            usersOnLocDao == expectedFriends

        cleanup:
            jdbcTemplate.execute(DELETE_LOCATION_BY_NAME)
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL)
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL_2)
    }

    def "should throw LocationNotFoundException if all my locations not have location with specified id"() {

        given:
            def owner = new User("test@gmail.com", "test", "pass")
            CompletableFuture<User> savedUserFuture = userService.saveUser(owner)
                .thenCompose({ result -> userService.findUserById(owner.getId()) })

            def savedOwner = savedUserFuture.join()

            def location = new Location("name", "address", savedOwner.getId())

            CompletableFuture<Location> savedLocationFuture = locationService.saveLocation(location, savedOwner.getId())
                .thenCompose({ result2 -> locationService.findLocationById(location.getId()) })

            def savedLocation = savedLocationFuture.join()

            def user2 = new User("test2@gmail.com", "test", "pass")
            CompletableFuture<User> savedUser2Future = userService.saveUser(user2)
                .thenCompose({ result3 -> userService.findUserById(user2.getId()) })

            def savedUser2 = savedUser2Future.join()

        when:
            def result = mockMvc.perform(get("/location/{locationId}/", savedLocation.getId())
                .cookie(new Cookie("user", savedUser2.getId().toString())))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("errorMessage", "Location not found"))

        then:
            List<User> usersOnLocService = userService.findAllUsersOnLocation(savedLocation.getId(), savedUser2.getId()).join()
            usersOnLocService.isEmpty()

        and:
            List<User> usersOnLocDaoAdmin = userDao.findAllUsersOnLocation(savedLocation.getId(), savedUser2.getId()).join()
            usersOnLocDaoAdmin.isEmpty()

        cleanup:
            jdbcTemplate.execute(DELETE_LOCATION_BY_NAME)
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL)
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL_2)
    }


    def "should change user access successfully"() {

        given:
            def owner = new User("test@gmail.com", "test", "pass")
            CompletableFuture<User> savedUserFuture = userService.saveUser(owner)
                .thenCompose({ result -> userService.findUserById(owner.getId()) })

            def savedOwner = savedUserFuture.join()

            def location = new Location("name", "address", savedOwner.getId())

            CompletableFuture<Location> savedLocationFuture = locationService.saveLocation(location, savedOwner.getId())
                .thenCompose({ result2 -> locationService.findLocationById(location.getId()) })

            def savedLocation = savedLocationFuture.join()

            def friend = new User("test2@gmail.com", "test", "pass")
            CompletableFuture<User> savedUser2Future = userService.saveUser(friend)
                .thenCompose({ result3 -> userService.findUserById(friend.getId()) })

            def savedFriend = savedUser2Future.join()

            def userAccess = new UserAccess("ADMIN", savedFriend.getId(), savedLocation.getId())
            userAccessService.saveUserAccess(userAccess).join()

        when:
            def mvcResult = mockMvc.perform(put("/location/change")
                .cookie(new Cookie("user", savedOwner.getId().toString()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userAccess)))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect { result ->
                    def jsonSlurper = new JsonSlurper()
                    def jsonResponse = jsonSlurper.parseText(mvcResult.response.contentAsString)
                    jsonResponse['title'] == 'READ'
                    jsonResponse['locationId'] == userAccess.getLocationId()
                    jsonResponse['userId'] == userAccess.getUserId()
                }
        then:
            UserAccess changedAccessService = userAccessService.findUserAccess(userAccess, savedOwner.getId()).join()
            changedAccessService.getTitle() == "READ"

        and:
            UserAccess changedAccessDao = userAccessDao.findUserAccess(userAccess, savedOwner.getId()).join()
            changedAccessDao.getTitle() == "READ"

        cleanup:
            jdbcTemplate.execute(DELETE_LOCATION_BY_NAME)
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL)
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL_2)
    }

    def "should throw UserAccessNotFoundException when user access not found"() {

        given:
            def owner = new User("test@gmail.com", "test", "pass")
            CompletableFuture<User> savedUserFuture = userService.saveUser(owner)
                .thenCompose({ result -> userService.findUserById(owner.getId()) })

            def savedOwner = savedUserFuture.join()

            def location = new Location("name", "address", savedOwner.getId())

            CompletableFuture<Location> savedLocationFuture = locationService.saveLocation(location, savedOwner.getId())
                .thenCompose({ result2 -> locationService.findLocationById(location.getId()) })

            def savedLocation = savedLocationFuture.join()

            def friend = new User("test2@gmail.com", "test", "pass")
            CompletableFuture<User> savedUser2Future = userService.saveUser(friend)
                .thenCompose({ result3 -> userService.findUserById(friend.getId()) })

            def savedFriend = savedUser2Future.join()

            def userAccess = new UserAccess("ADMIN", savedFriend.getId(), savedLocation.getId())

        when:
            def result = mockMvc.perform(put("/location/change")
                .cookie(new Cookie("user", savedOwner.getId().toString()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userAccess)))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("errorMessage", "User access not found"))

        then:
            0 * userAccessService.changeUserAccess(userAccess)
            0 * userAccessDao.changeUserAccess(userAccess)

        cleanup:
            jdbcTemplate.execute(DELETE_LOCATION_BY_NAME)
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL)
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL_2)
    }

    def "should delete location successfully"() {

        given:
            def user = new User("test@gmail.com", "test", "pass")

            CompletableFuture<User> savedUserFuture = userService.saveUser(user)
                .thenCompose({ result -> userService.findUserById(user.getId()) })

            def savedUser = savedUserFuture.join()

            def location = new Location("name", "address", savedUser.getId())

            CompletableFuture<Location> savedLocationFuture = locationService.saveLocation(location, savedUser.getId())
                .thenCompose({ result2 -> locationService.findLocationById(location.getId()) })

            def savedLocation = savedLocationFuture.join()

        when:
            def result = mockMvc.perform(delete("/location/delete/{name}/", savedLocation.getName())
                .cookie(new Cookie("user", savedUser.getId().toString())))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())

        then:
            def deletedLocationService = locationService.deleteLocation(savedLocation.getName(), savedUser.getId())
                .thenCompose({ result4 -> locationService.findLocationByNameAndUserId(savedLocation.getName(), savedLocation.getUserId()) })
                .join()
            deletedLocationService.isEmpty()

        and:
            def deletedLocationDao = locationService.deleteLocation(savedLocation.getName(), savedUser.getId())
                .thenCompose({ result5 -> locationService.findLocationByNameAndUserId(savedLocation.getName(), savedLocation.getUserId()) })
                .join()
            deletedLocationDao.isEmpty()

        cleanup:
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL)
    }

    def "should throw LocationOwnerNotFoundException when location owner not found for deleting location"() {

        given:
            def owner = new User("test@gmail.com", "test", "pass")

            CompletableFuture<User> savedOwnerFuture = userService.saveUser(owner)
                .thenCompose({ result -> userService.findUserById(owner.getId()) })

            def savedOwner = savedOwnerFuture.join()

            def location = new Location("name", "address", savedOwner.getId())


        when:
            def result = mockMvc.perform(delete("/location/delete/{name}/", location.getName())
                .cookie(new Cookie("user", savedOwner.getId().toString())))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("errorMessage", "Location owner not found"))

        then:
            0 * locationService.deleteLocation(location.getId(), savedOwner.getId())
            0 * locationDao.deleteLocation(location.getId(), savedOwner.getId())

        cleanup:
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL)
    }
}


