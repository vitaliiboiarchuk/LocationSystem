package com.example.locationsystem.location

import com.example.locationsystem.user.User
import com.example.locationsystem.user.UserControllerExceptions
import com.example.locationsystem.user.UserService
import com.example.locationsystem.userAccess.UserAccess
import com.example.locationsystem.userAccess.UserAccessService
import com.fasterxml.jackson.databind.ObjectMapper
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import javax.servlet.http.Cookie
import java.util.concurrent.CompletableFuture

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@AutoConfigureMockMvc
@SpringBootTest
class LocationControllerTest extends Specification {

    @Autowired
    MockMvc mockMvc

    @SpringBean
    LocationService locationService = Mock()

    @SpringBean
    UserService userService = Mock()

    @SpringBean
    UserAccessService userAccessService = Mock()

    User user
    User user2
    Location addedLoc
    Location accessLoc
    List<Location> addedLocs
    List<Location> accessLocs
    List<Location> allLocs
    Location location
    UserAccess userAccess
    List<Location> locsToShare

    def setup() {

        user = new User(1L, "user1", "name1", "pass1")
        user2 = new User(2L, "user2", "name2", "pass2")
        location = new Location(1L, "name1", "add1", user)
        userAccess = new UserAccess(1L, "ADMIN", user2, location)

        addedLoc = new Location(1L, "name1", "add1", user)
        accessLoc = new Location(3L, "name3", "add3", user2)

        addedLocs = [addedLoc]
        accessLocs = [accessLoc]

        allLocs = addedLocs + accessLocs

        locsToShare = [location]
    }

    def "should return user id from the request cookie"() {

        given:
            def request = new MockHttpServletRequest()
            def cookie = new Cookie("user", user.getId().toString())
            request.setCookies(cookie)

        and:
            def controller = new LocationController(userService, locationService, userAccessService)

        when:
            def result = controller.getUserIdFromRequest(request)

        then:
            result == user.getId()
    }

    def "should throw NotLoggedInException when user cookie is not present in the request"() {

        given:
            def request = new MockHttpServletRequest()

        and:
            def controller = new LocationController(userService, locationService, userAccessService)

        when:
            def exception = null
            try {
                controller.getUserIdFromRequest(request)
            } catch (UserControllerExceptions.NotLoggedInException ex) {
                exception = ex
            }

        then:
            exception != null
            exception.message == "Not logged in"
    }

    def "should show my locations"() {

        given:
            locationService.findAllMyLocations(user.getId()) >> CompletableFuture.completedFuture(allLocs)

        expect:
            def result = mockMvc.perform(get("/location")
                .cookie(new Cookie("user", user.getId().toString())))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(allLocs), true))
    }

    def "should show friends on location if all my locations have location with specified id"() {

        given:
            def accessUsers = [user2]
            locationService.findAllMyLocations(user.getId()) >> CompletableFuture.completedFuture(allLocs)
            userService.findAllUsersWithAccessOnLocation(1L, user.getId()) >> CompletableFuture.completedFuture(accessUsers)

        expect:
            def result = mockMvc.perform(get("/location/{locationId}/", 1L)
                .cookie(new Cookie("user", user.getId().toString())))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(accessUsers), true))
    }

    def "should throw NoLocationFoundException if all my locations not have location with specified id"() {

        given:
            locationService.findAllMyLocations(user.getId()) >> CompletableFuture.completedFuture(allLocs)
            userService.findAllUsersWithAccessOnLocation(2L, user.getId()) >> CompletableFuture.completedFuture(null)

        expect:
            def result = mockMvc.perform(get("/location/{locationId}/", 2L)
                .cookie(new Cookie("user", user.getId().toString())))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("errorMessage", "No location found"))
    }

    def "should throw LocationOwnerNotFoundException when location owner not found for changing user access"() {

        given:
            userService.findLocationOwner(1L, user.getId()) >> CompletableFuture.completedFuture(null)
            userAccessService.findUserAccess(1L, 2L) >> CompletableFuture.completedFuture(null)

        when:
            def result = mockMvc.perform(put("/location/change/{locationId}/{uId}/", 1L, 2L)
                .cookie(new Cookie("user", user.getId().toString())))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("errorMessage", "Failed to change access"))

        then:
            0 * userAccessService.changeUserAccess(1L, 2L)
    }

    def "should throw UserAccessNotFoundException when user access not found"() {

        given:
            userService.findLocationOwner(1L, user.getId()) >> CompletableFuture.completedFuture(user)
            userAccessService.findUserAccess(1L, 2L) >> CompletableFuture.completedFuture(null)

        when:
            def result = mockMvc.perform(put("/location/change/{locationId}/{uId}/", 1L, 2L)
                .cookie(new Cookie("user", user.getId().toString())))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("errorMessage", "User access not found"))

        then:
            0 * userAccessService.changeUserAccess(1L, 2L)
    }

    def "should change user access when location owner is found"() {

        given:
            userService.findLocationOwner(1L, user.getId()) >> CompletableFuture.completedFuture(user)
            userAccessService.findUserAccess(1L, 2L) >> CompletableFuture.completedFuture(userAccess)
            userAccessService.changeUserAccess(1L, 2L) >> CompletableFuture.completedFuture(null)

        when:
            def result = mockMvc.perform(put("/location/change/{locationId}/{uId}/", 1L, 2L)
                .cookie(new Cookie("user", user.getId().toString())))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(header().string("message", "Access changed successfully"))

        then:
            1 * userAccessService.changeUserAccess(1L, 2L)
    }

    def "should throw AlreadyExistsException when location already exists"() {

        given:
            locationService.findLocationByNameAndUserId(location.getName(), user.getId()) >> CompletableFuture.completedFuture(location)

        when:
            def result = mockMvc.perform(post("/location/add")
                .cookie(new Cookie("user", user.getId().toString()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(location)))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("errorMessage", "Location with that name already exists"))

        then:
            0 * locationService.saveLocation(location)
    }

    def "should throw EmptyFieldException when fields empty"() {

        given:
            def location = new Location(1L, "", "add1", user)
            locationService.findLocationByNameAndUserId(location.getName(), user.getId()) >> CompletableFuture.completedFuture(null)

        when:
            def result = mockMvc.perform(post("/location/add")
                .cookie(new Cookie("user", user.getId().toString()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(location)))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("errorMessage", "Fields can not be empty"))

        then:
            0 * locationService.saveLocation(location)
    }

    def "should add location successfully"() {

        given:
            locationService.findLocationByNameAndUserId(location.getName(), user.getId()) >> CompletableFuture.completedFuture(null)
            locationService.saveLocation(location) >> CompletableFuture.completedFuture(null)
            userService.findById(user.getId()) >> CompletableFuture.completedFuture(user)

        when:
            def result = mockMvc.perform(post("/location/add")
                .cookie(new Cookie("user", user.getId().toString()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(location)))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(location), true))

        then:
            1 * locationService.saveLocation(location)
    }

    def "should throw NoLocationToShareException when no location to share"() {

        given:
            def locsToShare = []
            locationService.findNotSharedToUserLocations(user.getId(), user2.getId()) >> CompletableFuture.completedFuture(locsToShare)
            userService.findById(user2.getId()) >> CompletableFuture.completedFuture(user2)

        when:
            def result = mockMvc.perform(post("/location/share/{locationId}/{uId}/", location.getId(), user2.getId())
                .cookie(new Cookie("user", user.getId().toString()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userAccess)))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("errorMessage", "No location to share"))

        then:
            0 * userAccessService.saveUserAccess(userAccess)
    }

    def "should throw NoUserToShareException when no user to share"() {

        given:
            locationService.findNotSharedToUserLocations(user.getId(), user2.getId()) >> CompletableFuture.completedFuture(locsToShare)
            userService.findById(user2.getId()) >> CompletableFuture.completedFuture(null)

        when:
            def result = mockMvc.perform(post("/location/share/{locationId}/{uId}/", location.getId(), user2.getId())
                .cookie(new Cookie("user", user.getId().toString()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userAccess)))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("errorMessage", "No user to share"))

        then:
            0 * userAccessService.saveUserAccess(userAccess)
    }

    def "should share location successfully"() {

        given:
            locationService.findNotSharedToUserLocations(user.getId(), user2.getId()) >> CompletableFuture.completedFuture(locsToShare)
            userService.findById(user2.getId()) >> CompletableFuture.completedFuture(user2)
            locationService.findById(location.getId()) >> CompletableFuture.completedFuture(location)
            userAccessService.saveUserAccess(userAccess) >> CompletableFuture.completedFuture(null)

        when:
            def result = mockMvc.perform(post("/location/share/{locationId}/{uId}/", location.getId(), user2.getId())
                .cookie(new Cookie("user", user.getId().toString()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userAccess)))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(userAccess), true))

        then:
            1 * userAccessService.saveUserAccess(userAccess)
    }

    def "should throw LocationOwnerNotFoundException when location owner not found for deleting location"() {

        given:
            userService.findLocationOwner(1L, user.getId()) >> CompletableFuture.completedFuture(null)

        when:
            def result = mockMvc.perform(delete("/location/delete/{locationId}/", 1L)
                .cookie(new Cookie("user", user.getId().toString())))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("errorMessage", "Failed to delete location"))

        then:
            0 * locationService.deleteLocation(1L, user.getId())
    }

    def "should delete location successfully"() {

        given:
            userService.findLocationOwner(1L, user.getId()) >> CompletableFuture.completedFuture(user)
            locationService.deleteLocation(1L, user.getId()) >> CompletableFuture.completedFuture(null)

        when:
            def result = mockMvc.perform(delete("/location/delete/{locationId}/", 1L)
                .cookie(new Cookie("user", user.getId().toString())))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(header().string("message", "Location deleted successfully"))

        then:
            1 * locationService.deleteLocation(1L, user.getId())
    }
}
