package com.example.locationsystem.user

import com.fasterxml.jackson.databind.ObjectMapper
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification
import org.springframework.http.MediaType

import javax.servlet.http.Cookie
import java.util.concurrent.CompletableFuture

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@AutoConfigureMockMvc
@SpringBootTest
class UserControllerTest extends Specification {

    @Autowired
    MockMvc mockMvc

    @SpringBean
    UserService userService = Mock()

    User user

    def setup() {

        user = new User(30L, "exists@gmail.com", "user30", "pass30")
    }

    def "should throw UserAlreadyExistsException when user already exists"() {

        given:
            userService.findByUsername(user.getUsername()) >> CompletableFuture.completedFuture(user)

        when:
            def mvcResult = mockMvc.perform(post("/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(user)))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("errorMessage", "User already exists"))

        then:
            0 * userService.saveUser(user)
    }

    def "should throw EmptyFieldException when field is empty"() {

        given:
            def user = new User(30L, "", "user30", "pass30")
            userService.findByUsername(user.getUsername()) >> CompletableFuture.completedFuture(null)

        when:
            def mvcResult = mockMvc.perform(post("/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(user)))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("errorMessage", "Fields can not be empty"))

        then:
            0 * userService.saveUser(user)
    }

    def "should throw InvalidEmailException when email is invalid"() {

        given:
            def user = new User(30L, "boyar", "user30", "pass30")
            userService.findByUsername(user.getUsername()) >> CompletableFuture.completedFuture(null)

        when:
            def mvcResult = mockMvc.perform(post("/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(user)))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("errorMessage", "Invalid email format"))

        then:
            0 * userService.saveUser(user)
    }

    def "should register user successfully"() {

        given:
            userService.findByUsername(user.getUsername()) >> CompletableFuture.completedFuture(null)
            userService.saveUser(user) >> CompletableFuture.completedFuture(null)

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
            1 * userService.saveUser(user)
    }

    def "should throw InvalidLoginOrPasswordException when login or password is invalid"() {

        given:
            userService.findUserByUsernameAndPassword(user.getUsername(), user.getPassword()) >> CompletableFuture.completedFuture(null)

        expect:
            def mvcResult = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(user)))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("errorMessage", "Invalid login or password"))
    }

    def "should login successfully"() {

        given:
            userService.findUserByUsernameAndPassword(user.getUsername(), user.getPassword()) >> CompletableFuture.completedFuture(user)

        expect:
            def mvcResult = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(user)))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("user"))
                .andExpect(content().json(new ObjectMapper().writeValueAsString(user), true))
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
}


