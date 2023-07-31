package com.example.locationsystem.user

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import javax.sql.DataSource

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

    JdbcTemplate jdbcTemplate

    @Autowired
    DataSource dataSource

    void setup() {

        jdbcTemplate = new JdbcTemplate(dataSource)
    }

    private static final String DELETE_USER_BY_EMAIL = "DELETE FROM users WHERE username = 'test@gmail.com';"
    private static final String DELETE_EVENT = "DELETE FROM history WHERE object_id = ?;"

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
                .andExpect { result ->
                    def jsonSlurper = new JsonSlurper()
                    def jsonResponse = jsonSlurper.parseText(mvcResult.response.contentAsString)
                    jsonResponse['username'] == user.getUsername()
                    jsonResponse['name'] == user.getName()
                    jsonResponse['password'] == user.getPassword()
                }

        then:
            Optional<User> savedUserService = userService.findUserByEmail(user.getUsername()).join()
            savedUserService.isPresent()
            savedUserService.get().getUsername() == user.getUsername()

        and:
            Optional<User> savedUserDao = userDao.findUserByEmail(user.getUsername()).join()
            savedUserDao.isPresent()
            savedUserDao.get().getUsername() == user.getUsername()

        cleanup:
            jdbcTemplate.update(DELETE_EVENT, savedUserService.get().getId())
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL)
    }

    def "should throw UserAlreadyExistsException when user already exists"() {

        given:
            User user = new User("test@gmail.com", "test", "pass")
            def savedUser = userService.saveUser(user).join()

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
            0 * userService.findUserByEmail(user.getUsername())
            0 * userService.saveUser(user)

        and:
            0 * userDao.findUserByEmail(user.getUsername())
            0 * userDao.saveUser(user)

        cleanup:
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL)
            jdbcTemplate.update(DELETE_EVENT, savedUser.getId())
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
            def savedUser = userService.saveUser(user).join()

        when:
            def mvcResult = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(user)))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("user"))
                .andExpect { result ->
                    def jsonSlurper = new JsonSlurper()
                    def jsonResponse = jsonSlurper.parseText(mvcResult.response.contentAsString)
                    jsonResponse['username'] == user.getUsername()
                    jsonResponse['name'] == user.getName()
                }
        then:
            User validUserService = userService.findUserByEmailAndPassword(user.getUsername(), user.getPassword()).join()
            validUserService.getUsername() == user.getUsername()

        and:
            User validUserDao = userDao.findUserByEmailAndPassword(user.getUsername(), user.getPassword()).join()
            validUserDao.getUsername() == user.getUsername()

        cleanup:
            jdbcTemplate.update(DELETE_EVENT, savedUser.getId())
            jdbcTemplate.execute(DELETE_USER_BY_EMAIL)
    }

    def "should throw InvalidLoginOrPasswordException when login or password is invalid"() {

        given:
            User wrongPassUser = new User("test@gmail.com", "test", "wrongPass")

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
            User user = new User("test@gmail.com", "test", "pass")
            def savedUser = userService.saveUser(user).join()

        when:
            def result = mockMvc.perform(delete("/delete/{username}/", savedUser.getUsername()))
                .andExpect(request().asyncStarted())
                .andReturn()

            mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())

        then:
            Optional<User> deletedUserService = userService.findUserByEmail(user.getUsername()).join()
            deletedUserService.isEmpty()

        and:
            Optional<User> deletedUserDao = userDao.findUserByEmail(user.getUsername()).join()
            deletedUserDao.isEmpty()

        cleanup:
            jdbcTemplate.update(DELETE_EVENT, savedUser.getId())
    }
}
