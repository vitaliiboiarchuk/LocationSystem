package com.example.locationsystem.user

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import javax.servlet.http.Cookie
import java.util.concurrent.CompletableFuture

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*


@AutoConfigureMockMvc
@SpringBootTest
class UserControllerTest extends Specification {

    @Autowired
    MockMvc mockMvc

    def "should display home page with welcome message when cookie is not present"() {
        expect:
        def mvcResult = mockMvc.perform(get("/"))
                .andExpect(request().asyncStarted())
                .andReturn()

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(view().name("homePage"))
                .andExpect(model().attributeExists("welcomePage"))
                .andExpect(model().attributeDoesNotExist("myProfile"))
    }

    def "should display home page when cookie is present"() {
        expect:
        def mvcResult = mockMvc.perform(get("/").cookie(new Cookie("user", "1234")))
                .andExpect(request().asyncStarted())
                .andReturn()

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(view().name("homePage"))
                .andExpect(model().attributeDoesNotExist("welcomePage"))
                .andExpect(model().attributeExists("myProfile"))
    }

    def "should return registration view"() {
        expect:
        mockMvc.perform(get("/registration"))
                .andExpect(status().isOk())
                .andExpect(view().name("entry/registration"))
                .andExpect(model().attributeExists("user"))
    }

    def "should return login view"() {
        expect:
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("entry/login"))
                .andExpect(model().attributeExists("user"))
    }

    def "should return to home page when logging out"() {
        expect:
        def mvcResult = mockMvc.perform(get("/logout"))
                .andExpect(request().asyncStarted())
                .andReturn()

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"))
    }

    def "should redirect to registration page with error when registration fails"() {
        given:
        def user = new User(30L, "exists@gmail.com", "user30", "pass30")
        def userService = Mock(UserService)
        userService.findByUsername(user.getUsername()) >> CompletableFuture.completedFuture(user)
        userService.saveUser(user) >> CompletableFuture.completedFuture(null)

        expect:
        def mvcResult = mockMvc.perform(post("/registration")
                .param("username", user.getUsername())
                .param("password", user.getPassword())
                .param("name", user.getName()))
                .andExpect(request().asyncStarted())
                .andReturn()

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registration?error=true"))
    }

    def "should redirect to login page when registration is successful"() {
        given:
        def user = new User(30L, "notExists@gmail.com", "user30", "pass30")
        def userService = Mock(UserService)
        userService.findByUsername(user.getUsername()) >> CompletableFuture.completedFuture(user)
        userService.saveUser(user) >> CompletableFuture.completedFuture(null)

        expect:
        def mvcResult = mockMvc.perform(post("/registration")
                .param("username", user.getUsername())
                .param("password", user.getPassword())
                .param("name", user.getName()))
                .andExpect(request().asyncStarted())
                .andReturn()

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
    }

    def "should redirect to login page with error when login fails"() {
        given:
        def user = new User(30L, "notExists@gmail.com", "use30", "notRight")
        def userService = Mock(UserService)
        userService.findUserByUsernameAndPassword(user.getUsername(),user.getPassword()) >> user

        expect:
        def mvcResult = mockMvc.perform(post("/login")
                .param("username", user.getUsername())
                .param("password", user.getPassword()))
                .andExpect(request().asyncStarted())
                .andReturn()

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"))
    }


    def "should redirect to home page when login successful"() {
        given:
        def user = new User(30L, "notExists@gmail.com", "use30", "pass30")
        def userService = Mock(UserService)
        userService.findUserByUsernameAndPassword(user.getUsername(),user.getPassword()) >> user

        expect:
        def mvcResult = mockMvc.perform(post("/login")
                .param("username", user.getUsername())
                .param("password", user.getPassword()))
                .andExpect(request().asyncStarted())
                .andReturn()

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
    }
}


