package com.example.locationsystem.user

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.Specification

@SpringBootTest
class UserDaoTest extends Specification {

    @Autowired
    UserDao userDao

    @Autowired
    JdbcTemplate jdbcTemplate

    def setup() {

        jdbcTemplate.execute("INSERT INTO users(id,name,username,password) VALUES(100,'name1','user1',SHA2('pass1',256))")
        jdbcTemplate.execute("INSERT INTO locations(id,address,name,user_id) VALUES(100,'Add1','name1',100)")
    }

    def cleanup() {

        jdbcTemplate.execute("DELETE FROM locations WHERE name = 'name1'")
        jdbcTemplate.execute("DELETE FROM users WHERE name = 'name1'")
    }

    def "should find user by email"() {

        when:
            def user = userDao.findUserByEmail("user1").join()

        then:
            user.isPresent()
            user.get().getUsername() == 'user1'
    }

    def "should find user by email and password"() {

        when:
            def user = userDao.findUserByEmailAndPassword("user1", "pass1").join()

        then:
            user.getUsername() == 'user1'
    }

    def "should save user"() {

        given:
            def user = new User(username: "user4", name: "name4", password: "pass4")

        when:
            def savedUserId = userDao.saveUser(user).join()

        then:
            def savedUser = userDao.findUserById(savedUserId).join()
            savedUser.getUsername() == user.getUsername()
            savedUser.getName() == user.getName()

        cleanup:
            jdbcTemplate.execute("DELETE FROM users WHERE username = 'user4'")
    }

    def "should find all users with access on location"() {

        given:
            jdbcTemplate.execute("INSERT INTO users(id,name,username,password) VALUES(200,'name2','user2',SHA2('pass2',256)),(300,'name3','user3',SHA2('pass3',256))")
            jdbcTemplate.execute("INSERT INTO accesses(id,title,location_id,user_id) VALUES(100,'ADMIN',100,300),(200,'ADMIN',100,200)")

        when:
            def userIds = userDao.findAllUsersOnLocation(100, 300).join()

        then:
            userIds.size() == 1
            def userId = userIds[0]
            def user = userDao.findUserById(userId).join()
            user.getUsername() == 'user2'

        cleanup:
            jdbcTemplate.execute("DELETE FROM accesses WHERE id = 100")
            jdbcTemplate.execute("DELETE FROM accesses WHERE id = 200")
            jdbcTemplate.execute("DELETE FROM users WHERE id = 200")
            jdbcTemplate.execute("DELETE FROM users WHERE id = 300")
    }

    def "should delete user by email"() {

        given:
            jdbcTemplate.execute("INSERT INTO users(id,name,username,password) VALUES(200,'name1','test@gmail.com',SHA2('pass1',256))")

        when:
            userDao.deleteUserByEmail("test@gmail.com").join()

        then:
            def deletedUser = jdbcTemplate.query("SELECT * FROM users WHERE username = ?", BeanPropertyRowMapper.newInstance(User.class), "test@gmail.com")
            deletedUser.isEmpty()
    }

    def "should find user by id"() {

        when:
            def user = userDao.findUserById(100L).join()

        then:
            user.getUsername() == 'user1'
    }
}
