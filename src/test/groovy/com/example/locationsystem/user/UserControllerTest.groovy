package com.example.locationsystem.user

import com.fasterxml.jackson.databind.ObjectMapper
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification
import org.springframework.http.MediaType

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

    def "should return user registration failed message when user already exists"() {

        given:
            def user = new User(30L, "exists@gmail.com", "user30", "pass30")
            userService.findByUsername(user.getUsername()) >> CompletableFuture.completedFuture(user)

        expect:
            def mvcResult = mockMvc.perform(post("/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(user)))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User already exists"))
    }

    def "should return user registration failed message when field is empty"() {

        given:
            def user = new User(30L, "", "user30", "pass30")
            userService.findByUsername(user.getUsername()) >> CompletableFuture.completedFuture(null)

        expect:
            def mvcResult = mockMvc.perform(post("/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(user)))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Fields username and password can not be empty"))
    }

    def "should return user registration failed message when invalid email format"() {

        given:
            def user = new User(30L, "boyar", "user30", "pass30")
            userService.findByUsername(user.getUsername()) >> CompletableFuture.completedFuture(null)

        expect:
            def mvcResult = mockMvc.perform(post("/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(user)))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid email format"))
    }

    def "should register user successfully"() {

        given:
            def user = new User(30L, "test@gmail.com", "user30", "pass30")
            userService.findByUsername(user.getUsername()) >> CompletableFuture.completedFuture(null)
            userService.saveUser(user) >> CompletableFuture.completedFuture(null)

        expect:
            def mvcResult = mockMvc.perform(post("/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(user)))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully"))
    }

    def "should return log in failed message when login fails"() {

        given:
            def user = new User(30L, "test@gmail.com", "use30", "pass")
            userService.findUserByUsernameAndPassword(user.getUsername(), user.getPassword()) >> CompletableFuture.completedFuture(null)

        expect:
            def mvcResult = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(user)))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Log in failed"))
    }

    def "should login successfully"() {

        given:
            def user = new User(30L, "test@gmail.com", "use30", "pass30")
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
                .andExpect(content().string("Logged in successfully"))
    }

    def "should log out successfully"() {

        expect:
            def mvcResult = mockMvc.perform(get("/logout"))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist("user"))
                .andExpect(content().string("Logged out successfully"))
    }
}


