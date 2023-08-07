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
    private static final String DELETE_EVENT = "DELETE FROM history WHERE object_id = ?;"

    def "should add location successfully"() {

        given:
            def user = new User("test@gmail.com", "test", "pass")
            def savedUserId = userService.saveUser(user).join()
            def location = new Location("name", "address", savedUserId)

        when:
            def mvcResult = mockMvc.perform(post("/location/add")
                .cookie(new Cookie("user", savedUserId.toString()))
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
                savedUserId).join()
            addedLocationService.get().getName() == location.getName()
            addedLocationService.get().getAddress() == location.getAddress()

        and:
            Optional<Location> addedLocationDao = locationDao.findLocationByNameAndUserId(location.getName(),
                savedUserId).join()
            addedLocationDao.get().getName() == location.getName()
            addedLocationDao.get().getAddress() == location.getAddress()

        cleanup:
            jdbcTemplate.execute(DELETE_LOCATION_BY_NAME)
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL)
            jdbcTemplate.update(DELETE_EVENT, savedUserId)
            jdbcTemplate.update(DELETE_EVENT, addedLocationService.get().getId())
    }

    def "should throw MethodArgumentNotValidException when field is empty"() {

        given:
            def user = new User("test@gmail.com", "test", "pass")
            def savedUser = userService.saveUser(user).join()

            def emptyFieldLocation = new Location("", "add1", savedUser)

        when:
            def result = mockMvc.perform(post("/location/add")
                .cookie(new Cookie("user", savedUser.toString()))
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
            jdbcTemplate.update(DELETE_EVENT, savedUser)
    }

    def "should throw AlreadyExistsException when location already exists"() {

        given:
            def user = new User("test@gmail.com", "test", "pass")
            def savedUserId = userService.saveUser(user).join()

            def location = new Location("name", "address", savedUserId)
            def savedLocation = locationService.saveLocation(location, savedUserId).join()

        when:
            def result = mockMvc.perform(post("/location/add")
                .cookie(new Cookie("user", savedUserId.toString()))
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
            jdbcTemplate.update(DELETE_EVENT, savedUserId)
            jdbcTemplate.update(DELETE_EVENT, savedLocation.getId())
    }

    def "should show my locations"() {

        given:
            def user = new User("test@gmail.com", "test", "pass")
            def savedUserId = userService.saveUser(user).join()

            def location = new Location("name", "address", savedUserId)
            def savedLocation = locationService.saveLocation(location, savedUserId).join()

            def user2 = new User("test2@gmail.com", "test", "pass")
            def savedUser2Id = userService.saveUser(user2).join()

            def location2 = new Location("name2", "address", savedUser2Id)
            def savedLocation2 = locationService.saveLocation(location2, savedUser2Id).join()

            def userAccess = new UserAccess("ADMIN", savedUserId, savedLocation2.getId())
            def savedAccess = userAccessService.saveUserAccess(userAccess).join()

            def expectedLocs = [savedLocation, savedLocation2]

        expect:
            def mvcResult = mockMvc.perform(get("/location")
                .cookie(new Cookie("user", savedUserId.toString())))
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
            jdbcTemplate.update(DELETE_EVENT, savedUserId)
            jdbcTemplate.update(DELETE_EVENT, savedUser2Id)
            jdbcTemplate.update(DELETE_EVENT, savedLocation.getId())
            jdbcTemplate.update(DELETE_EVENT, savedLocation2.getId())
            jdbcTemplate.update(DELETE_EVENT, savedAccess.getId())
    }

    def "should share location successfully"() {

        given:
            def user = new User("test@gmail.com", "test", "pass")
            def savedUserId = userService.saveUser(user).join()

            def location = new Location("name", "address", savedUserId)
            def savedLocation = locationService.saveLocation(location, savedUserId).join()

            def user2 = new User("test2@gmail.com", "test", "pass")
            def savedUser2Id = userService.saveUser(user2).join()

            def userAccess = new UserAccess("ADMIN", savedUser2Id, savedLocation.getId())

        when:
            def mvcResult = mockMvc.perform(post("/location/share")
                .cookie(new Cookie("user", savedUserId.toString()))
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
            UserAccess addedUserAccessDao = userAccessDao.findUserAccess(userAccess, savedUserId).join()
            userAccess.getTitle() == addedUserAccessDao.getTitle()

        cleanup:
            jdbcTemplate.execute(DELETE_LOCATION_BY_NAME)
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL)
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL_2)
            jdbcTemplate.update(DELETE_EVENT, addedUserAccessDao.getId())
            jdbcTemplate.update(DELETE_EVENT, savedUserId)
            jdbcTemplate.update(DELETE_EVENT, savedUser2Id)
            jdbcTemplate.update(DELETE_EVENT, savedLocation.getId())
    }

    def "should throw NoLocationOrUserFoundException when no location or user found to share"() {

        given:
            def user = new User("test@gmail.com", "test", "pass")
            def savedUserId = userService.saveUser(user).join()

            def location = new Location("name", "address", savedUserId)
            def savedLocation = locationService.saveLocation(location, savedUserId).join()

            def userAccess = new UserAccess("ADMIN", savedUserId + 1, savedLocation.getId() + 1)

        when:
            def result = mockMvc.perform(post("/location/share")
                .cookie(new Cookie("user", savedUserId.toString()))
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
            jdbcTemplate.update(DELETE_EVENT, savedUserId)
            jdbcTemplate.update(DELETE_EVENT, savedLocation.getId())
    }

    def "should show friends on location if all my locations have location with specified id"() {

        given:
            def owner = new User("test@gmail.com", "test", "pass")
            def savedOwnerId = userService.saveUser(owner).join()

            def location = new Location("name", "address", savedOwnerId)
            def savedLocation = locationService.saveLocation(location, savedOwnerId).join()

            def friend = new User("test2@gmail.com", "test", "pass")
            def savedFriend = userService.saveUser(friend)
                .thenCompose({ result -> userService.findUserById(result) }).join()

            def userAccess = new UserAccess("ADMIN", savedFriend.getId(), savedLocation.getId())
            def savedAccess = userAccessService.saveUserAccess(userAccess).join()

            def expectedFriends = [savedFriend]

        expect:
            def mvcResult = mockMvc.perform(get("/location/{locationId}/", savedLocation.getId())
                .cookie(new Cookie("user", savedOwnerId.toString())))
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

        cleanup:
            jdbcTemplate.execute(DELETE_LOCATION_BY_NAME)
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL)
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL_2)
            jdbcTemplate.update(DELETE_EVENT, savedOwnerId)
            jdbcTemplate.update(DELETE_EVENT, savedLocation.getId())
            jdbcTemplate.update(DELETE_EVENT, savedFriend.getId())
            jdbcTemplate.update(DELETE_EVENT, savedAccess.getId())
    }

    def "should throw LocationNotFoundException if all my locations not have location with specified id"() {

        given:
            def owner = new User("test@gmail.com", "test", "pass")
            def savedOwnerId = userService.saveUser(owner).join()

            def location = new Location("name", "address", savedOwnerId)
            def savedLocation = locationService.saveLocation(location, savedOwnerId).join()

            def user2 = new User("test2@gmail.com", "test", "pass")
            def savedUser2Id = userService.saveUser(user2).join()

        when:
            def result = mockMvc.perform(get("/location/{locationId}/", savedLocation.getId())
                .cookie(new Cookie("user", savedUser2Id.toString())))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("errorMessage", "Location not found"))

        then:
            List<User> usersOnLocService = userService.findAllUsersOnLocation(savedLocation.getId(), savedUser2Id).join()
            usersOnLocService.isEmpty()

        and:
            List<User> usersOnLocDaoAdmin = userDao.findAllUsersOnLocation(savedLocation.getId(), savedUser2Id).join()
            usersOnLocDaoAdmin.isEmpty()

        cleanup:
            jdbcTemplate.execute(DELETE_LOCATION_BY_NAME)
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL)
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL_2)
            jdbcTemplate.update(DELETE_EVENT, savedOwnerId)
            jdbcTemplate.update(DELETE_EVENT, savedUser2Id)
            jdbcTemplate.update(DELETE_EVENT, savedLocation.getId())
    }

    def "should change user access successfully"() {

        given:
            def owner = new User("test@gmail.com", "test", "pass")
            def savedOwnerId = userService.saveUser(owner).join()

            def location = new Location("name", "address", savedOwnerId)
            def savedLocation = locationService.saveLocation(location, savedOwnerId).join()

            def friend = new User("test2@gmail.com", "test", "pass")
            def savedFriendId = userService.saveUser(friend).join()

            def userAccess = new UserAccess("ADMIN", savedFriendId, savedLocation.getId())
            def savedAccess = userAccessService.saveUserAccess(userAccess).join()

        when:
            def mvcResult = mockMvc.perform(put("/location/change")
                .cookie(new Cookie("user", savedOwnerId.toString()))
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
            UserAccess changedAccessDao = userAccessDao.findUserAccess(userAccess, savedOwnerId).join()
            changedAccessDao.getTitle() == "READ"

        cleanup:
            jdbcTemplate.execute(DELETE_LOCATION_BY_NAME)
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL)
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL_2)
            jdbcTemplate.update(DELETE_EVENT, savedOwnerId)
            jdbcTemplate.update(DELETE_EVENT, savedLocation.getId())
            jdbcTemplate.update(DELETE_EVENT, savedAccess.getId())
            jdbcTemplate.update(DELETE_EVENT, savedFriendId)
    }

    def "should throw UserAccessNotFoundException when user access not found"() {

        given:
            def owner = new User("test@gmail.com", "test", "pass")
            def savedOwnerId = userService.saveUser(owner).join()

            def location = new Location("name", "address", savedOwnerId)
            def savedLocation = locationService.saveLocation(location, savedOwnerId).join()

            def friend = new User("test2@gmail.com", "test", "pass")
            def savedFriendId = userService.saveUser(friend).join()

            def userAccess = new UserAccess("ADMIN", savedFriendId, savedLocation.getId())

        when:
            def result = mockMvc.perform(put("/location/change")
                .cookie(new Cookie("user", savedOwnerId.toString()))
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
            jdbcTemplate.update(DELETE_EVENT, savedOwnerId)
            jdbcTemplate.update(DELETE_EVENT, savedLocation.getId())
            jdbcTemplate.update(DELETE_EVENT, savedFriendId)
    }

    def "should delete location successfully"() {

        given:
            def user = new User("test@gmail.com", "test", "pass")
            def savedUserId = userService.saveUser(user).join()

            def location = new Location("name", "address", savedUserId)
            def savedLocation = locationService.saveLocation(location, savedUserId).join()

        when:
            def result = mockMvc.perform(delete("/location/delete/{name}/", savedLocation.getName())
                .cookie(new Cookie("user", savedUserId.toString())))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())

        then:
            def deletedLocationDao = locationDao.deleteLocation(savedLocation.getName(), savedUserId)
                .thenCompose({ result5 -> locationDao.findLocationByNameAndUserId(savedLocation.getName(), savedLocation.getUserId()) })
                .join()
            deletedLocationDao.isEmpty()

        cleanup:
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL)
            jdbcTemplate.update(DELETE_EVENT, savedLocation.getId())
            jdbcTemplate.update(DELETE_EVENT, savedUserId)
    }

    def "should throw LocationNotFoundException when location owner not found for deleting location"() {

        given:
            def owner = new User("test@gmail.com", "test", "pass")
            def savedOwnerId = userService.saveUser(owner).join()

            def location = new Location("name", "address", savedOwnerId)

        when:
            def result = mockMvc.perform(delete("/location/delete/{name}/", location.getName())
                .cookie(new Cookie("user", savedOwnerId.toString())))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("errorMessage", "Location not found"))

        then:
            0 * locationService.deleteLocation(location.getId(), savedOwnerId)
            0 * locationDao.deleteLocation(location.getId(), savedOwnerId)

        cleanup:
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL)
            jdbcTemplate.update(DELETE_EVENT, savedOwnerId)
    }
}


