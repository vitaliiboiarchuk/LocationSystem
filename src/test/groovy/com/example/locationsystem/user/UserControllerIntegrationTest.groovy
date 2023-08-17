package com.example.locationsystem.user

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Shared
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@AutoConfigureMockMvc
@SpringBootTest
class UserControllerIntegrationTest extends Specification {

    @Shared
    def user = new User(username: "test@gmail.com", name: "test", password: "pass")

    @Autowired
    MockMvc mockMvc

    @Autowired
    UserService userService

    @Autowired
    JdbcTemplate jdbcTemplate

    private static final String DELETE_USER_BY_EMAIL = "DELETE FROM users WHERE username = 'test@gmail.com';"
    private static final String DELETE_EVENT = "DELETE FROM history WHERE object_id = ?;"

    def "should register user successfully"() {

        when:
            def mvcResult = mockMvc.perform(post("/registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(user)))
                .andExpect(request().asyncStarted())
                .andReturn()

        then:
            def savedUser = userService.findUserByEmail(user.getUsername()).join()

            mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andDo { result ->
                    def expectedId = result.response.contentAsString
                    savedUser.isPresent()
                    savedUser.get().getId() == expectedId
                }

        cleanup:
            jdbcTemplate.update(DELETE_EVENT, savedUser.get().getId())
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL)
    }

    def "should throw UserAlreadyExistsException when user already exists"() {

        given:
            def savedUserId = userService.saveUser(user).join()

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

        cleanup:
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL)
            jdbcTemplate.update(DELETE_EVENT, savedUserId)
    }

    def "should throw MethodArgumentNotValidException when field is empty"() {

        given:
            def user = new User(username: "", name: "test", password: "pass")

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
    }

    def "should throw MethodArgumentNotValidException when invalid email format"() {

        given:
            def user = new User(username: "test", name: "test", password: "pass")

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
    }

    def "should login successfully"() {

        given:
            def savedUserId = userService.saveUser(user).join()

        when:
            def mvcResult = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(user)))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("user"))
                .andDo { result ->
                    def expectedId = result.response.contentAsString
                    savedUserId == expectedId
                }

        then:
            def validUser = userService.findUserByEmailAndPassword(user.getUsername(), user.getPassword()).join()
            validUser.getUsername() == user.getUsername()

        cleanup:
            jdbcTemplate.update(DELETE_EVENT, savedUserId)
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL)
    }

    def "should throw InvalidLoginOrPasswordException when login or password is invalid"() {

        given:
            def wrongPassUser = new User(username: "test@gmail.com", name: "test", password: "wrongPass")

        expect:
            def mvcResult = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(wrongPassUser)))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("errorMessage", "Invalid login or password"))
    }

    def "should delete user successfully"() {

        given:
            def savedUser = userService.saveUser(user)
                .thenCompose({ result -> userService.findUserById(result) }).join()

        when:
            def result = mockMvc.perform(delete("/delete/{username}/", savedUser.getUsername()))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())

        then:
            def deletedUser = userService.findUserByEmail(user.getUsername()).join()
            deletedUser.isEmpty()

        cleanup:
            jdbcTemplate.update(DELETE_EVENT, savedUser.getId())
    }
}
