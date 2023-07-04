package com.example.locationsystem.user

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import javax.servlet.http.Cookie
import java.util.concurrent.CompletableFuture

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@AutoConfigureMockMvc
@SpringBootTest
class UserControllerIntegrationTest extends Specification {

    @Autowired
    MockMvc mockMvc

    @Autowired
    UserService userService

    @Autowired
    UserDao userDao

    def "should register user successfully"() {

        given:
            User user = new User("test@gmail.com", "test", "pass")

        when:
            def mvcResult = mockMvc.perform(post("/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(user)))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(user), true))

        then:
            User savedUserService = userService.findByUsername(user.getUsername()).join()
            savedUserService != null
            savedUserService.getUsername() == user.getUsername()

        and:
            User savedUserDao = userDao.findByUsername(user.getUsername()).join()
            savedUserDao != null
            savedUserDao.getUsername() == user.getUsername()

        cleanup:
            userService.deleteUserByUsername(user.getUsername()).join()
    }

    def "should throw UserAlreadyExistsException when user already exists"() {

        given:
            User user = new User("test@gmail.com", "test", "pass")
            CompletableFuture<User> savedUserFuture = userService.saveUser(user)
                .thenComposeAsync({ result -> userService.findByUsername(user.getUsername()) })

            def savedUser = savedUserFuture.join()

        when:
            def mvcResult = mockMvc.perform(post("/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(savedUser)))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("errorMessage", "User already exists"))

        then:
            User savedUserService = userService.findByUsername(user.getUsername()).join()
            savedUserService != null
            savedUserService.getUsername() == user.getUsername()
            0 * userService.saveUser(user)

        and:
            User savedUserDao = userDao.findByUsername(user.getUsername()).join()
            savedUserDao != null
            savedUserDao.getUsername() == user.getUsername()
            0 * userDao.saveUser(user)

        cleanup:
            userService.deleteUserByUsername(user.getUsername()).join()
    }

    def "should throw MethodArgumentNotValidException when field is empty"() {

        given:
            User user = new User("", "test", "pass")

        when:
            def mvcResult = mockMvc.perform(post("/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andReturn()

        then:
            def errorMessage = mvcResult.response.getHeader("errorMessage")
            errorMessage == "Field can not be empty"
            0 * userService.saveUser(user)
            0 * userDao.saveUser(user)
    }

    def "should throw MethodArgumentNotValidException when invalid email format"() {

        given:
            User user = new User("test", "test", "pass")

        when:
            def mvcResult = mockMvc.perform(post("/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(user)))
                .andExpect(status().isBadRequest())
                .andReturn()

        then:
            def errorMessage = mvcResult.response.getHeader("errorMessage")
            errorMessage == "Invalid email format"
            0 * userService.saveUser(user)
            0 * userDao.saveUser(user)
    }

    def "should login successfully"() {

        given:
            User user = new User("test@gmail.com", "test", "pass")
            CompletableFuture<User> savedUserFuture = userService.saveUser(user)
                .thenComposeAsync({ result -> userService.findByUsername(user.getUsername()) })

            def savedUser = savedUserFuture.join()

        when:
            def mvcResult = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(user)))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("user"))
                .andExpect(content().json(new ObjectMapper().writeValueAsString(user), true))

        then:
            User validUserService = userService.findUserByUsernameAndPassword(user.getUsername(), user.getPassword()).join()
            validUserService.getUsername() == user.getUsername()

        and:
            User validUserDao = userDao.findUserByUsernameAndPassword(user.getUsername(), user.getPassword()).join()
            validUserDao.getUsername() == user.getUsername()

        cleanup:
            userService.deleteUserByUsername(savedUser.getUsername()).join()
    }

    def "should throw InvalidLoginOrPasswordException when login or password is invalid"() {

        given:
            User user = new User("test@gmail.com", "test", "pass")
            User wrongPassUser = new User("test@gmail.com", "test", "wrongPass")

        when:
            def mvcResult = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(wrongPassUser)))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("errorMessage", "Invalid login or password"))

        then:
            User notValidUserService = userService.findUserByUsernameAndPassword(wrongPassUser.getUsername(), wrongPassUser.getPassword()).join()
            notValidUserService != user

        and:
            User notValidUserDao = userService.findUserByUsernameAndPassword(wrongPassUser.getUsername(), wrongPassUser.getPassword()).join()
            notValidUserDao != user
    }

    def "should log out successfully"() {

        expect:
            def mvcResult = mockMvc.perform(get("/logout").cookie(new Cookie("user", "1")))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("user", 0))
                .andExpect(header().string("message", "Logged out successfully"))
    }

    def "should throw NotLoggedInException when user not logged in"() {

        expect:
            def mvcResult = mockMvc.perform(get("/logout"))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist("user"))
                .andExpect(header().string("errorMessage", "Not logged in"))
    }

    def "should delete user successfully"() {

        given:
            User user = new User("test@gmail.com", "test", "pass")
            CompletableFuture<User> savedUserFuture = userService.saveUser(user)
                .thenComposeAsync({ result -> userService.findByUsername(user.getUsername()) })

            def savedUser = savedUserFuture.join()

        when:
            def result = mockMvc.perform(delete("/delete/{username}/", savedUser.getUsername()))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(header().string("message", "User deleted successfully"))

        then:
            User deletedUserService = userService.findByUsername(user.getUsername()).join()
            deletedUserService == null

        and:
            User deletedUserDao = userDao.findByUsername(user.getUsername()).join()
            deletedUserDao == null
    }
}
