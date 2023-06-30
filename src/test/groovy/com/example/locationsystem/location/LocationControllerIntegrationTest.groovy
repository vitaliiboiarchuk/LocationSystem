package com.example.locationsystem.location

import com.example.locationsystem.exception.ControllerExceptions
import com.example.locationsystem.user.User
import com.example.locationsystem.user.UserDao
import com.example.locationsystem.user.UserService
import com.example.locationsystem.userAccess.UserAccess
import com.example.locationsystem.userAccess.UserAccessDao
import com.example.locationsystem.userAccess.UserAccessService
import com.fasterxml.jackson.databind.ObjectMapper
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

    def "should return user id from the request cookie"() {

        given:
            def user = new User()
            user.setId(1L)
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
            } catch (ControllerExceptions.NotLoggedInException ex) {
                exception = ex
            }

        then:
            exception != null
            exception.message == "Not logged in"
    }

    def "should add location successfully"() {

        given:
            def user = new User("test@gmail.com", "test", "pass")
            CompletableFuture<User> savedUserFuture = userService.saveUser(user)
                .thenComposeAsync({ result -> userService.findByUsername(user.getUsername()) })

            def savedUser = savedUserFuture.join()

            def location = new Location("name", "address", savedUser)

        when:
            def result = mockMvc.perform(post("/location/add")
                .cookie(new Cookie("user", savedUser.getId().toString()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(location)))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(location), true))

        then:
            Location addedLocationService = locationService.findLocationByNameAndUserId(location.getName(),
                savedUser.getId()).join()
            addedLocationService.getName() == location.getName()
            addedLocationService.getAddress() == location.getAddress()

        and:
            Location addedLocationDao = locationDao.findLocationByNameAndUserId(location.getName(),
                savedUser.getId()).join()
            addedLocationDao.getName() == location.getName()
            addedLocationDao.getAddress() == location.getAddress()

        cleanup:
            locationService.findLocationByName(location.getName())
                .thenComposeAsync({ result2 -> locationService.deleteLocation(result2.getId(), savedUser.getId()) })
                .thenComposeAsync({ deletedLocation -> userService.deleteUserByUsername(savedUser.getUsername()) })
                .join()
    }

    def "should throw MethodArgumentNotValidException when field is empty"() {

        given:
            def user = new User("test@gmail.com", "test", "pass")
            CompletableFuture<User> savedUserFuture = userService.saveUser(user)
                .thenComposeAsync({ result -> userService.findByUsername(user.getUsername()) })

            def savedUser = savedUserFuture.join()

            def emptyFieldLocation = new Location("", "add1", savedUser)

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
            savedUserFuture
                .thenComposeAsync({ result2 -> userService.deleteUserByUsername(savedUser.getUsername()) })
    }

    def "should throw AlreadyExistsException when location already exists"() {

        given:
            def user = new User("test@gmail.com", "test", "pass")
            CompletableFuture<User> savedUserFuture = userService.saveUser(user)
                .thenComposeAsync({ result -> userService.findByUsername(user.getUsername()) })

            def savedUser = savedUserFuture.join()

            def location = new Location("name", "address", savedUser)

            CompletableFuture<Location> savedLocationFuture = locationService.saveLocation(location)
                .thenComposeAsync({ result2 -> locationService.findLocationByName(location.getName()) })

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
            savedLocationFuture
                .thenComposeAsync({ result3 -> locationService.deleteLocation(result3.getId(), savedUser.getId()) })
                .thenComposeAsync({ deletedLocation -> userService.deleteUserByUsername(savedUser.getUsername()) })
                .join()
    }

    def "should show my locations"() {

        given:
            def user = new User("test@gmail.com", "test", "pass")
            CompletableFuture<User> savedUserFuture = userService.saveUser(user)
                .thenComposeAsync({ result -> userService.findByUsername(user.getUsername()) })

            def savedUser = savedUserFuture.join()

            def location = new Location("name", "address", savedUser)

            CompletableFuture<Location> savedLocationFuture = locationService.saveLocation(location)
                .thenComposeAsync({ result2 -> locationService.findLocationByName(location.getName()) })

            def savedLocation = savedLocationFuture.join()

            def user2 = new User("test2@gmail.com", "test", "pass")
            CompletableFuture<User> savedUser2Future = userService.saveUser(user2)
                .thenComposeAsync({ result3 -> userService.findByUsername(user2.getUsername()) })

            def savedUser2 = savedUser2Future.join()

            def location2 = new Location("name2", "address", savedUser2)

            CompletableFuture<Location> savedLocation2Future = locationService.saveLocation(location2)
                .thenComposeAsync({ result4 -> locationService.findLocationByName(location2.getName()) })

            def savedLocation2 = savedLocation2Future.join()

            def userAccess = new UserAccess("ADMIN", savedUser, savedLocation2)
            userAccessService.saveUserAccess(userAccess).join()

            def allLocations = [savedLocation, savedLocation2]

        expect:
            def result = mockMvc.perform(get("/location")
                .cookie(new Cookie("user", savedUser.getId().toString())))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(allLocations), true))

        cleanup:
            savedLocation2Future
                .thenComposeAsync({ result5 -> locationService.deleteLocation(result5.getId(), savedUser2.getId()) })
                .thenComposeAsync({ result6 -> locationService.deleteLocation(savedLocation.getId(), savedUser.getId()) })
                .thenComposeAsync({ result7 -> userService.deleteUserByUsername(savedUser2.getUsername()) })
                .thenComposeAsync({ result8 -> userService.deleteUserByUsername(savedUser.getUsername()) })
                .join()
    }

    def "should share location successfully"() {

        given:
            def user = new User("test@gmail.com", "test", "pass")
            CompletableFuture<User> savedUserFuture = userService.saveUser(user)
                .thenComposeAsync({ result -> userService.findByUsername(user.getUsername()) })

            def savedUser = savedUserFuture.join()

            def location = new Location("name", "address", savedUser)

            CompletableFuture<Location> savedLocationFuture = locationService.saveLocation(location)
                .thenComposeAsync({ result2 -> locationService.findLocationByName(location.getName()) })

            def savedLocation = savedLocationFuture.join()

            def user2 = new User("test2@gmail.com", "test", "pass")
            CompletableFuture<User> savedUser2Future = userService.saveUser(user2)
                .thenComposeAsync({ result3 -> userService.findByUsername(user2.getUsername()) })

            def savedUser2 = savedUser2Future.join()

            savedLocation.setUser(savedUser)
            def userAccess = new UserAccess("ADMIN", savedUser2, savedLocation)

        when:
            def result = mockMvc.perform(post("/location/share/{locationId}/{uId}/", savedLocation.getId(), savedUser2.getId())
                .cookie(new Cookie("user", savedUser.getId().toString()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userAccess)))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(userAccess), true))

        then:
            UserAccess addedUserAccessService = userAccessService.findUserAccess(savedLocation.getId(), savedUser2.getId()).join()
            userAccess.getTitle() == addedUserAccessService.getTitle()

        and:
            UserAccess addedUserAccessDao = userAccessDao.findUserAccess(savedLocation.getId(), savedUser2.getId()).join()
            userAccess.getTitle() == addedUserAccessDao.getTitle()

        cleanup:
            savedLocationFuture
                .thenComposeAsync({ result4 -> locationService.deleteLocation(result4.getId(), savedUser.getId()) })
                .thenComposeAsync({ result5 -> userService.deleteUserByUsername(savedUser.getUsername()) })
                .thenComposeAsync({ result6 -> userService.deleteUserByUsername(savedUser2.getUsername()) })
                .join()
    }

    def "should throw NoLocationToShareException when no location to share"() {

        given:
            def user = new User("test@gmail.com", "test", "pass")
            CompletableFuture<User> savedUserFuture = userService.saveUser(user)
                .thenComposeAsync({ result -> userService.findByUsername(user.getUsername()) })

            def savedUser = savedUserFuture.join()

            def location = new Location("name", "address", savedUser)

            CompletableFuture<Location> savedLocationFuture = locationService.saveLocation(location)
                .thenComposeAsync({ result2 -> locationService.findLocationByName(location.getName()) })

            def savedLocation = savedLocationFuture.join()

            def user2 = new User("test2@gmail.com", "test", "pass")
            CompletableFuture<User> savedUser2Future = userService.saveUser(user2)
                .thenComposeAsync({ result3 -> userService.findByUsername(user2.getUsername()) })

            def savedUser2 = savedUser2Future.join()

            savedLocation.setUser(savedUser)
            def userAccess = new UserAccess("ADMIN", savedUser2, savedLocation)
            userAccessService.saveUserAccess(userAccess).join()

        when:
            def result = mockMvc.perform(post("/location/share/{locationId}/{uId}/", savedLocation.getId(), savedUser2.getId())
                .cookie(new Cookie("user", savedUser.getId().toString()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userAccess)))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("errorMessage", "No location to share"))

        then:
            0 * userAccessService.saveUserAccess(userAccess)
            0 * userAccessDao.saveUserAccess(userAccess)

        cleanup:
            savedLocationFuture
                .thenComposeAsync({ result4 -> locationService.deleteLocation(result4.getId(), savedUser.getId()) })
                .thenComposeAsync({ result5 -> userService.deleteUserByUsername(savedUser.getUsername()) })
                .thenComposeAsync({ result6 -> userService.deleteUserByUsername(savedUser2.getUsername()) })
                .join()
    }

    def "should throw NoUserToShareException when no user to share"() {

        given:
            def user = new User("test@gmail.com", "test", "pass")
            CompletableFuture<User> savedUserFuture = userService.saveUser(user)
                .thenComposeAsync({ result -> userService.findByUsername(user.getUsername()) })

            def savedUser = savedUserFuture.join()

            def location = new Location("name", "address", savedUser)

            CompletableFuture<Location> savedLocationFuture = locationService.saveLocation(location)
                .thenComposeAsync({ result2 -> locationService.findLocationByName(location.getName()) })

            def savedLocation = savedLocationFuture.join()

            def user2 = new User("test2@gmail.com", "test", "pass")
            CompletableFuture<User> savedUser2Future = userService.saveUser(user2)
                .thenComposeAsync({ result3 -> userService.findByUsername(user2.getUsername()) })

            def savedUser2 = savedUser2Future.join()

            savedLocation.setUser(savedUser)
            def userAccess = new UserAccess("ADMIN", savedUser2, savedLocation)

            def userId = userService.getMaxIdFromUsers() + 1

            userAccessService.saveUserAccess(userAccess).join()

        when:
            def result = mockMvc.perform(post("/location/share/{locationId}/{uId}/", savedLocation.getId(), userId)
                .cookie(new Cookie("user", savedUser.getId().toString()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userAccess)))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("errorMessage", "No user to share"))

        then:
            0 * userAccessService.saveUserAccess(userAccess)
            0 * userAccessDao.saveUserAccess(userAccess)

        cleanup:
            savedLocationFuture
                .thenComposeAsync({ result4 -> locationService.deleteLocation(result4.getId(), savedUser.getId()) })
                .thenComposeAsync({ result5 -> userService.deleteUserByUsername(savedUser.getUsername()) })
                .thenComposeAsync({ result6 -> userService.deleteUserByUsername(savedUser2.getUsername()) })
                .join()
    }

    def "should throw SelfShareException when sharing location to yourself"() {

        given:
            def user = new User("test@gmail.com", "test", "pass")
            CompletableFuture<User> savedUserFuture = userService.saveUser(user)
                .thenComposeAsync({ result -> userService.findByUsername(user.getUsername()) })

            def savedUser = savedUserFuture.join()

            def location = new Location("name", "address", savedUser)

            CompletableFuture<Location> savedLocationFuture = locationService.saveLocation(location)
                .thenComposeAsync({ result2 -> locationService.findLocationByName(location.getName()) })

            def savedLocation = savedLocationFuture.join()

            savedLocation.setUser(savedUser)
            def userAccess = new UserAccess("ADMIN", savedUser, savedLocation)

        when:
            def result = mockMvc.perform(post("/location/share/{locationId}/{uId}/", savedLocation.getId(), savedUser.getId())
                .cookie(new Cookie("user", savedUser.getId().toString()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userAccess)))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("errorMessage", "Can't share to yourself"))

        then:
            0 * userAccessService.saveUserAccess(userAccess)
            0 * userAccessDao.saveUserAccess(userAccess)

        cleanup:
            savedLocationFuture
                .thenComposeAsync({ result4 -> locationService.deleteLocation(result4.getId(), savedUser.getId()) })
                .thenComposeAsync({ result5 -> userService.deleteUserByUsername(savedUser.getUsername()) })
                .join()
    }

    def "should show friends on location if all my locations have location with specified id"() {

        given:
            def owner = new User("test@gmail.com", "test", "pass")
            CompletableFuture<User> savedUserFuture = userService.saveUser(owner)
                .thenComposeAsync({ result -> userService.findByUsername(owner.getUsername()) })

            def savedOwner = savedUserFuture.join()

            def location = new Location("name", "address", savedOwner)

            CompletableFuture<Location> savedLocationFuture = locationService.saveLocation(location)
                .thenComposeAsync({ result2 -> locationService.findLocationByName(location.getName()) })

            def savedLocation = savedLocationFuture.join()

            def friend = new User("test2@gmail.com", "test", "pass")
            CompletableFuture<User> savedUser2Future = userService.saveUser(friend)
                .thenComposeAsync({ result3 -> userService.findByUsername(friend.getUsername()) })

            def savedFriend = savedUser2Future.join()

            savedLocation.setUser(savedOwner)
            def userAccess = new UserAccess("ADMIN", savedFriend, savedLocation)
            CompletableFuture<UserAccess> savedUserAccessFuture = userAccessService.saveUserAccess(userAccess)
                .thenComposeAsync({ result4 -> userAccessService.findUserAccess(savedLocation.getId(), savedFriend.getId()) })

            def savedUserAccess = savedUserAccessFuture.join()

            def friends = [savedFriend]

        when:
            def result = mockMvc.perform(get("/location/{locationId}/", savedLocation.getId())
                .cookie(new Cookie("user", savedOwner.getId().toString())))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(friends), true))

        then:
            List<User> usersOnLocService = userService.findAllUsersWithAccessOnLocation(savedLocation.getId(), savedOwner.getId()).join()
            usersOnLocService == friends

        and:
            List<User> usersOnLocDao = userDao.findAllUsersWithAccessOnLocation(savedLocation.getId(), savedUserAccess.getTitle(), savedOwner.getId()).join()
            usersOnLocDao == friends

        cleanup:
            savedLocationFuture
                .thenComposeAsync({ result5 -> locationService.deleteLocation(result5.getId(), savedOwner.getId()) })
                .thenComposeAsync({ result6 -> userService.deleteUserByUsername(savedOwner.getUsername()) })
                .thenComposeAsync({ result7 -> userService.deleteUserByUsername(savedFriend.getUsername()) })
                .join()
    }

    def "should throw NoLocationFoundException if all my locations not have location with specified id"() {

        given:
            def owner = new User("test@gmail.com", "test", "pass")
            CompletableFuture<User> savedUserFuture = userService.saveUser(owner)
                .thenComposeAsync({ result -> userService.findByUsername(owner.getUsername()) })

            def savedOwner = savedUserFuture.join()

            def location = new Location("name", "address", savedOwner)

            CompletableFuture<Location> savedLocationFuture = locationService.saveLocation(location)
                .thenComposeAsync({ result2 -> locationService.findLocationByName(location.getName()) })

            def savedLocation = savedLocationFuture.join()

            def user2 = new User("test2@gmail.com", "test", "pass")
            CompletableFuture<User> savedUser2Future = userService.saveUser(user2)
                .thenComposeAsync({ result3 -> userService.findByUsername(user2.getUsername()) })

            def savedUser2 = savedUser2Future.join()

            savedLocation.setUser(savedOwner)

        when:
            def result = mockMvc.perform(get("/location/{locationId}/", savedLocation.getId())
                .cookie(new Cookie("user", savedUser2.getId().toString())))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("errorMessage", "No location found"))

        then:
            List<User> usersOnLocService = userService.findAllUsersWithAccessOnLocation(savedLocation.getId(), savedUser2.getId()).join()
            usersOnLocService.isEmpty()

        and:
            List<User> usersOnLocDaoAdmin = userDao.findAllUsersWithAccessOnLocation(savedLocation.getId(), "ADMIN", savedUser2.getId()).join()
            List<User> usersOnLocDaoRead = userDao.findAllUsersWithAccessOnLocation(savedLocation.getId(), "READ", savedUser2.getId()).join()
            usersOnLocDaoAdmin.isEmpty()
            usersOnLocDaoRead.isEmpty()

        cleanup:
            savedLocationFuture
                .thenComposeAsync({ result5 -> locationService.deleteLocation(result5.getId(), savedOwner.getId()) })
                .thenComposeAsync({ result6 -> userService.deleteUserByUsername(savedOwner.getUsername()) })
                .thenComposeAsync({ result7 -> userService.deleteUserByUsername(savedUser2.getUsername()) })
                .join()
    }

    def "should change user access when location owner is found"() {

        given:
            def owner = new User("test@gmail.com", "test", "pass")
            CompletableFuture<User> savedUserFuture = userService.saveUser(owner)
                .thenComposeAsync({ result -> userService.findByUsername(owner.getUsername()) })

            def savedOwner = savedUserFuture.join()

            def location = new Location("name", "address", savedOwner)

            CompletableFuture<Location> savedLocationFuture = locationService.saveLocation(location)
                .thenComposeAsync({ result2 -> locationService.findLocationByName(location.getName()) })

            def savedLocation = savedLocationFuture.join()

            def friend = new User("test2@gmail.com", "test", "pass")
            CompletableFuture<User> savedUser2Future = userService.saveUser(friend)
                .thenComposeAsync({ result3 -> userService.findByUsername(friend.getUsername()) })

            def savedFriend = savedUser2Future.join()

            savedLocation.setUser(savedOwner)
            def userAccess = new UserAccess("ADMIN", savedFriend, savedLocation)
            userAccessService.saveUserAccess(userAccess).join()

        when:
            def result = mockMvc.perform(put("/location/change/{locationId}/{uId}/", savedLocation.getId(), savedFriend.getId())
                .cookie(new Cookie("user", savedOwner.getId().toString())))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(header().string("message", "Access changed successfully"))

        then:
            UserAccess changedAccessService = userAccessService.findUserAccess(savedLocation.getId(), savedFriend.getId()).join()
            changedAccessService.getTitle() == "READ"

        and:
            UserAccess changedAccessDao = userAccessDao.findUserAccess(savedLocation.getId(), savedFriend.getId()).join()
            changedAccessDao.getTitle() == "READ"

        cleanup:
            savedLocationFuture
                .thenComposeAsync({ result5 -> locationService.deleteLocation(result5.getId(), savedOwner.getId()) })
                .thenComposeAsync({ result6 -> userService.deleteUserByUsername(savedOwner.getUsername()) })
                .thenComposeAsync({ result7 -> userService.deleteUserByUsername(savedFriend.getUsername()) })
                .join()
    }

    def "should throw LocationOwnerNotFoundException when location owner not found for changing user access"() {

        given:
            def owner = new User("test@gmail.com", "test", "pass")
            CompletableFuture<User> savedUserFuture = userService.saveUser(owner)
                .thenComposeAsync({ result -> userService.findByUsername(owner.getUsername()) })

            def savedOwner = savedUserFuture.join()

            def location = new Location("name", "address", savedOwner)

            CompletableFuture<Location> savedLocationFuture = locationService.saveLocation(location)
                .thenComposeAsync({ result2 -> locationService.findLocationByName(location.getName()) })

            def savedLocation = savedLocationFuture.join()

            def friend = new User("test2@gmail.com", "test", "pass")
            CompletableFuture<User> savedUser2Future = userService.saveUser(friend)
                .thenComposeAsync({ result3 -> userService.findByUsername(friend.getUsername()) })

            def savedFriend = savedUser2Future.join()

            savedLocation.setUser(savedOwner)
            def userAccess = new UserAccess("ADMIN", savedFriend, savedLocation)
            userAccessService.saveUserAccess(userAccess).join()

        when:
            def result = mockMvc.perform(put("/location/change/{locationId}/{uId}/", savedLocation.getId(), savedFriend.getId())
                .cookie(new Cookie("user", savedFriend.getId().toString())))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("errorMessage", "Location owner not found"))

        then:
            0 * userAccessService.changeUserAccess(userAccess)
            0 * userAccessDao.changeUserAccess(userAccess)

        cleanup:
            savedLocationFuture
                .thenComposeAsync({ result5 -> locationService.deleteLocation(result5.getId(), savedOwner.getId()) })
                .thenComposeAsync({ result6 -> userService.deleteUserByUsername(savedOwner.getUsername()) })
                .thenComposeAsync({ result7 -> userService.deleteUserByUsername(savedFriend.getUsername()) })
                .join()
    }

    def "should throw UserAccessNotFoundException when user access not found"() {

        given:
            def owner = new User("test@gmail.com", "test", "pass")
            CompletableFuture<User> savedUserFuture = userService.saveUser(owner)
                .thenComposeAsync({ result -> userService.findByUsername(owner.getUsername()) })

            def savedOwner = savedUserFuture.join()

            def location = new Location("name", "address", savedOwner)

            CompletableFuture<Location> savedLocationFuture = locationService.saveLocation(location)
                .thenComposeAsync({ result2 -> locationService.findLocationByName(location.getName()) })

            def savedLocation = savedLocationFuture.join()

            def friend = new User("test2@gmail.com", "test", "pass")
            CompletableFuture<User> savedUser2Future = userService.saveUser(friend)
                .thenComposeAsync({ result3 -> userService.findByUsername(friend.getUsername()) })

            def savedFriend = savedUser2Future.join()

            savedLocation.setUser(savedOwner)
            def userAccess = new UserAccess("ADMIN", savedFriend, savedLocation)

        when:
            def result = mockMvc.perform(put("/location/change/{locationId}/{uId}/", savedLocation.getId(), savedFriend.getId())
                .cookie(new Cookie("user", savedOwner.getId().toString())))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("errorMessage", "User access not found"))

        then:
            0 * userAccessService.changeUserAccess(userAccess)
            0 * userAccessDao.changeUserAccess(userAccess)

        cleanup:
            savedLocationFuture
                .thenComposeAsync({ result5 -> locationService.deleteLocation(result5.getId(), savedOwner.getId()) })
                .thenComposeAsync({ result6 -> userService.deleteUserByUsername(savedOwner.getUsername()) })
                .thenComposeAsync({ result7 -> userService.deleteUserByUsername(savedFriend.getUsername()) })
                .join()
    }

    def "should delete location successfully"() {

        given:
            def user = new User("test@gmail.com", "test", "pass")

            CompletableFuture<User> savedUserFuture = userService.saveUser(user)
                .thenComposeAsync({ result -> userService.findByUsername(user.getUsername()) })

            def savedUser = savedUserFuture.join()

            def location = new Location("name", "address", savedUser)

            CompletableFuture<Location> savedLocationFuture = locationService.saveLocation(location)
                .thenComposeAsync({ result2 -> locationService.findLocationByName(location.getName()) })

            def savedLocation = savedLocationFuture.join()

        when:
            def result = mockMvc.perform(delete("/location/delete/{locationId}/", savedLocation.getId())
                .cookie(new Cookie("user", savedUser.getId().toString())))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(header().string("message", "Location deleted successfully"))

        then:
            def deletedLocationService = locationService.deleteLocation(savedLocation.getId(), savedUser.getId())
                .thenComposeAsync({ result4 -> locationService.findLocationByName(location.getName()) })
                .join()
            deletedLocationService == null

        and:
            def deletedLocationDao = locationService.deleteLocation(savedLocation.getId(), savedUser.getId())
                .thenComposeAsync({ result5 -> locationService.findLocationByName(location.getName()) })
                .join()
            deletedLocationDao == null

        cleanup:
            userService.findByUsername(savedUser.getUsername())
                .thenComposeAsync({ result3 -> userService.deleteUserByUsername(savedUser.getUsername()) })
    }

    def "should throw LocationOwnerNotFoundException when location owner not found for deleting location"() {

        given:
            def owner = new User("test@gmail.com", "test", "pass")

            CompletableFuture<User> savedOwnerFuture = userService.saveUser(owner)
                .thenComposeAsync({ result -> userService.findByUsername(owner.getUsername()) })

            def savedOwner = savedOwnerFuture.join()

            def location = new Location("name", "address", savedOwner)

            CompletableFuture<Location> savedLocationFuture = locationService.saveLocation(location)
                .thenComposeAsync({ result2 -> locationService.findLocationByName(location.getName()) })

            def savedLocation = savedLocationFuture.join()

            def notOwner = new User("notOwner@gmail.com", "test", "pass")

            CompletableFuture<User> notOwnerSavedFuture = userService.saveUser(notOwner)
                .thenComposeAsync({ result3 -> userService.findByUsername(notOwner.getUsername()) })

            def notOwnerSaved = notOwnerSavedFuture.join()

        when:
            def result = mockMvc.perform(delete("/location/delete/{locationId}/", savedLocation.getId())
                .cookie(new Cookie("user", notOwnerSaved.getId().toString())))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("errorMessage", "Location owner not found"))

        then:
            0 * locationService.deleteLocation(savedLocation.getId(), notOwnerSaved.getId())
            0 * locationDao.deleteLocation(savedLocation.getId(), notOwnerSaved.getId())

        cleanup:
            savedLocationFuture
                .thenComposeAsync({ result4 -> locationService.deleteLocation(result4.getId(), savedOwner.getId()) })
                .thenComposeAsync({ deletedLocation -> userService.deleteUserByUsername(savedOwner.getUsername()) })
                .thenComposeAsync({ deletedUser1 -> userService.deleteUserByUsername(notOwnerSaved.getUsername()) })
                .join()
    }
}


