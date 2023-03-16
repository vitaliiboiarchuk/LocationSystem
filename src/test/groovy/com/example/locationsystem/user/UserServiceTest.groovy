package com.example.locationsystem.user

import com.example.locationsystem.location.Location
import com.example.locationsystem.role.RoleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import spock.lang.Specification

@SpringBootTest
class UserServiceTest extends Specification {

    @Autowired
    UserService userService

    @Autowired
    UserRepository userRepository

    @Autowired
    RoleRepository roleRepository

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder

    def user1 = new User("test@gmail.com", "test", "1234", 1)
    def user2 = new User("test2@gmail.com", "test", "1234", 1)
    def user3 = new User("test3@gmail.com", "test", "1234", 1)

    def location = new Location("test", "test", user1)

    def users = new ArrayList<>()


    void setup() {
        userRepository = Mock()
        roleRepository = Mock()
        bCryptPasswordEncoder = new BCryptPasswordEncoder()
        userService = new UserServiceImpl(userRepository, roleRepository, bCryptPasswordEncoder)

        user1.setId(1L)
        user2.setId(2L)
        user3.setId(3L)

        location.setId(1L)

    }

    def "should find by username"() {
        given:
        userRepository.findByUsername(user1.username) >> user1

        when:
        User result = userService.findByUserName(user1.username)

        then:
        user1.getUsername() == result.getUsername()
    }

    def "should find users to share locations"() {
        given:
        users << user1
        userRepository.findAllByIdNotLike(user2.id) >> users

        when:
        List<User> result = userService.findUsersToShare(user2.id)

        then:
        users == result
    }

    def "should find all users with access on location"() {
        given:
        users << user2
        users << user3
        def users2 = new ArrayList<>()
        users2 << user3
        userRepository.findAllUsersWithAccessOnLocation(location.id, "READ") >> users

        when:
        List<User> result = userService.findAllUsersWithAccessOnLocation(location.id, "READ", user2.id)

        then:
        users2 == result
    }

    def "should find location owner"() {
        given:
        userRepository.findUserByLocationId(location.id) >> user1

        when:
        User result = userService.findLocationOwner(location.id,user1.id)
        User result2 = userService.findLocationOwner(location.id,user2.id)

        then:
        result == null
        result2 == user1
    }
}

