package com.example.locationsystem.user

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*


@AutoConfigureMockMvc
@SpringBootTest
class UserControllerTest extends Specification {

    @Autowired
    MockMvc mockMvc

    def "when get is performed then status is 200 and should show home page"() {
        expect:
        mockMvc.perform(get("/"))
                .andExpect(view().name("home"))
                .andExpect(status().isOk())
    }

    def "when get is performed then status is 200 and should show login page"() {
        expect:
        mockMvc.perform(get("/login"))
                .andExpect(model().attributeExists("user"))
                .andExpect(view().name("entry/login"))
                .andExpect(status().isOk())
    }

    def "when get is performed then status is 200 and should show registration page"() {
        expect:
        mockMvc.perform(get("/registration"))
                .andExpect(model().attributeExists("user"))
                .andExpect(view().name("entry/registration"))
                .andExpect(status().isOk())
    }

    def "when post is performed then data valid and should save user"() {
        expect:
        mockMvc.perform(post("/registration")
                .param("name", "test")
                .param("username", "test")
                .param("password", "test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/login"))
    }

    def "when post is performed then data not valid and should return to registration page"() {
        expect:
        mockMvc.perform(post("/registration")
                .param("name", "")
                .param("username", "")
                .param("password", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("entry/registration"))
    }
}

