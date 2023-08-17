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
import spock.lang.Shared
import spock.lang.Specification

import javax.servlet.http.Cookie

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@AutoConfigureMockMvc
@SpringBootTest
class LocationControllerIntegrationTest extends Specification {

    @Shared
    def user = new User(username: "test@gmail.com", name: "test", password: "pass")

    @Shared
    def user2 = new User(username: "test2@gmail.com", name: "test", password: "pass")

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

    @Autowired
    JdbcTemplate jdbcTemplate

    private static final String DELETE_LOCATION_BY_NAME = "DELETE FROM locations WHERE name = 'name';"
    private static final String DELETE_LOCATION_BY_NAME_2 = "DELETE FROM locations WHERE name = 'name2';"
    private static final String DELETE_USER_BY_EMAIL = "DELETE FROM users WHERE username = 'test@gmail.com';"
    private static final String DELETE_USER_BY_EMAIL_2 = "DELETE FROM users WHERE username = 'test2@gmail.com';"
    private static final String DELETE_EVENT = "DELETE FROM history WHERE object_id = ?;"

    def "should add location successfully"() {

        given:
            def savedUserId = userService.saveUser(user).join()

            def location = new Location(name: "name", address: "address", userId: savedUserId)

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
                    def jsonSlurp = new JsonSlurper()
                    def jsonResponse = jsonSlurp.parseText(mvcResult.response.contentAsString)
                    jsonResponse['name'] == location.getName()
                    jsonResponse['address'] == location.getAddress()
                }

        then:
            Optional<Location> addedLocation = locationService.findLocationByNameAndUserId(location.getName(),
                savedUserId).join()
            addedLocation.get().getName() == location.getName()
            addedLocation.get().getAddress() == location.getAddress()

        cleanup:
            jdbcTemplate.execute(DELETE_LOCATION_BY_NAME)
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL)
            jdbcTemplate.update(DELETE_EVENT, savedUserId)
            jdbcTemplate.update(DELETE_EVENT, addedLocation.get().getId())
    }

    def "should throw MethodArgumentNotValidException when field is empty"() {

        given:
            def savedUser = userService.saveUser(user).join()

            def emptyFieldLocation = new Location(name: "", address: "add1", userId: savedUser)

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

        cleanup:
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL)
            jdbcTemplate.update(DELETE_EVENT, savedUser)
    }

    def "should throw AlreadyExistsException when location already exists"() {

        given:
            def savedUserId = userService.saveUser(user).join()

            def location = new Location(name: "name", address: "address", userId: savedUserId)
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

        cleanup:
            jdbcTemplate.execute(DELETE_LOCATION_BY_NAME)
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL)
            jdbcTemplate.update(DELETE_EVENT, savedUserId)
            jdbcTemplate.update(DELETE_EVENT, savedLocation.getId())
    }

    def "should show my locations"() {

        given:
            def savedUserId = userService.saveUser(user).join()
            def savedUser2Id = userService.saveUser(user2).join()

            def location = new Location(name: "name", address: "address", userId: savedUserId)
            def savedLocation = locationService.saveLocation(location, savedUserId).join()

            def location2 = new Location(name: "name2", address: "address", userId: savedUser2Id)
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
                    def jsonSlurp = new JsonSlurper()
                    def jsonResponse = jsonSlurp.parseText(mvcResult.response.contentAsString)
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
            def savedUserId = userService.saveUser(user).join()
            def savedUser2Id = userService.saveUser(user2).join()

            def location = new Location(name: "name", address: "address", userId: savedUserId)
            def savedLocation = locationService.saveLocation(location, savedUserId).join()

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
                    def jsonSlurp = new JsonSlurper()
                    def jsonResponse = jsonSlurp.parseText(mvcResult.response.contentAsString)
                    jsonResponse['title'] == userAccess.getTitle()
                    jsonResponse['locationId'] == userAccess.getLocationId()
                    jsonResponse['userId'] == userAccess.getUserId()
                }
        then:
            UserAccess addedUserAccess = userAccessService.findUserAccess(userAccess, savedUserId).join()
            userAccess.getTitle() == addedUserAccess.getTitle()

        cleanup:
            jdbcTemplate.execute(DELETE_LOCATION_BY_NAME)
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL)
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL_2)
            jdbcTemplate.update(DELETE_EVENT, addedUserAccess.getId())
            jdbcTemplate.update(DELETE_EVENT, savedUserId)
            jdbcTemplate.update(DELETE_EVENT, savedUser2Id)
            jdbcTemplate.update(DELETE_EVENT, savedLocation.getId())
    }

    def "should throw LocationOrUserFoundException when no location or user found to share"() {

        given:
            def savedUserId = userService.saveUser(user).join()

            def location = new Location(name: "name", address: "address", userId: savedUserId)
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

        cleanup:
            jdbcTemplate.execute(DELETE_LOCATION_BY_NAME)
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL)
            jdbcTemplate.update(DELETE_EVENT, savedUserId)
            jdbcTemplate.update(DELETE_EVENT, savedLocation.getId())
    }

    def "should show friends on location if all my locations have location with specified id"() {

        given:
            def savedOwnerId = userService.saveUser(user).join()
            def savedFriendId = userService.saveUser(user2).join()

            def location = new Location(name: "name", address: "address", userId: savedOwnerId)
            def savedLocation = locationService.saveLocation(location, savedOwnerId).join()

            def userAccess = new UserAccess("ADMIN", savedFriendId, savedLocation.getId())
            def savedAccess = userAccessService.saveUserAccess(userAccess).join()

        expect:
            def mvcResult = mockMvc.perform(get("/location/{locationId}/", savedLocation.getId())
                .cookie(new Cookie("user", savedOwnerId.toString())))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andDo { result ->
                    def content = result.response.contentAsString
                    def objectMapper = new ObjectMapper()
                    def userIds = objectMapper.readValue(content, List.class)
                    userIds.size() == 1
                    userIds.contains([savedFriendId])
                }

        cleanup:
            jdbcTemplate.execute(DELETE_LOCATION_BY_NAME)
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL)
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL_2)
            jdbcTemplate.update(DELETE_EVENT, savedOwnerId)
            jdbcTemplate.update(DELETE_EVENT, savedLocation.getId())
            jdbcTemplate.update(DELETE_EVENT, savedFriendId)
            jdbcTemplate.update(DELETE_EVENT, savedAccess.getId())
    }

    def "should throw LocationNotFoundException if all my locations not have location with specified id"() {

        given:
            def savedOwnerId = userService.saveUser(user).join()
            def savedUser2Id = userService.saveUser(user2).join()

            def location = new Location(name: "name", address: "address", userId: savedOwnerId)
            def savedLocation = locationService.saveLocation(location, savedOwnerId).join()

        when:
            def result = mockMvc.perform(get("/location/{locationId}/", savedLocation.getId())
                .cookie(new Cookie("user", savedUser2Id.toString())))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("errorMessage", "Location not found"))

        then:
            List<Long> usersOnLoc = userService.findAllUsersOnLocation(savedLocation.getId(), savedUser2Id).join()
            usersOnLoc.isEmpty()

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
            def savedOwnerId = userService.saveUser(user).join()
            def savedFriendId = userService.saveUser(user2).join()

            def location = new Location(name: "name", address: "address", userId: savedOwnerId)
            def savedLocation = locationService.saveLocation(location, savedOwnerId).join()

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

        then:
            UserAccess changedAccess = userAccessService.findUserAccess(userAccess, savedOwnerId).join()
            changedAccess.getTitle() == "READ"

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
            def savedOwnerId = userService.saveUser(user).join()
            def savedFriendId = userService.saveUser(user2).join()

            def location = new Location(name: "name", address: "address", userId: savedOwnerId)
            def savedLocation = locationService.saveLocation(location, savedOwnerId).join()

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
            def savedUserId = userService.saveUser(user).join()

            def location = new Location(name: "name", address: "address", userId: savedUserId)
            def savedLocation = locationService.saveLocation(location, savedUserId).join()

        when:
            def result = mockMvc.perform(delete("/location/delete/{name}/", savedLocation.getName())
                .cookie(new Cookie("user", savedUserId.toString())))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())

        then:
            def deletedLocation = locationService.findLocationByNameAndUserId(savedLocation.getName(), savedLocation.getUserId()).join()
            deletedLocation.isEmpty()

        cleanup:
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL)
            jdbcTemplate.update(DELETE_EVENT, savedLocation.getId())
            jdbcTemplate.update(DELETE_EVENT, savedUserId)
    }

    def "should throw LocationNotFoundException when location owner not found for deleting location"() {

        given:
            def savedOwnerId = userService.saveUser(user).join()

            def location = new Location(name: "name", address: "address", userId: savedOwnerId)

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

        cleanup:
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL)
            jdbcTemplate.update(DELETE_EVENT, savedOwnerId)
    }
}


