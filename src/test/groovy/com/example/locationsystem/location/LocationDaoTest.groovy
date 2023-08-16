package com.example.locationsystem.location

import com.example.locationsystem.userAccess.UserAccess
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.Specification

@SpringBootTest
class LocationDaoTest extends Specification {

    @Autowired
    LocationDao locationDao

    @Autowired
    JdbcTemplate jdbcTemplate

    def setup() {

        jdbcTemplate.execute("INSERT INTO users(id,name,password,username) VALUES(100,'vitalii','pass1','vitalii'),(200,'natalia','pass2','natalia'),(300,'oleh','pass3','oleh')")
        jdbcTemplate.execute("INSERT INTO locations(id,name,address,user_id) VALUES (100, 'home','test',100),(200,'gym','naleczowska',100),(300,'swimming pool','sobieskiego',300)")
        jdbcTemplate.execute("INSERT INTO accesses(id,title,location_id,user_id) VALUES(100,'ADMIN',100,200),(200,'READ',300,100)")
    }

    def cleanup() {

        jdbcTemplate.execute("DELETE FROM accesses WHERE id = 100")
        jdbcTemplate.execute("DELETE FROM accesses WHERE id = 200")
        jdbcTemplate.execute("DELETE FROM locations WHERE id = 100")
        jdbcTemplate.execute("DELETE FROM locations WHERE id = 200")
        jdbcTemplate.execute("DELETE FROM locations WHERE id = 300")
        jdbcTemplate.execute("DELETE FROM users WHERE id = 100")
        jdbcTemplate.execute("DELETE FROM users WHERE id = 200")
        jdbcTemplate.execute("DELETE FROM users WHERE id = 300")
    }

    def "should find all user locations"() {

        when:
            def locations = locationDao.findAllUserLocations(100L).join()

        then:
            locations.size() == 3
            locations[0].getName() == 'swimming pool'
            locations[1].getName() == 'home'
            locations[2].getName() == 'gym'
    }

    def "should find location in user locations"() {

        when:
            def location = locationDao.findLocationInUserLocations(100L, 100L).join()

        then:
            location.getName() == 'home'
            location.getAddress() == 'test'
    }

    def "should find location by name and userId"() {

        when:
            def location = locationDao.findLocationByNameAndUserId("gym", 100L).join()

        then:
            location.isPresent()
            location.get().getAddress() == 'naleczowska'
    }

    def "should save location"() {

        given:
            def location = new Location(name: "title1", address: "add1", userId: 100L)

        when:
            def loc = locationDao.saveLocation(location).join()

        then:
            loc.getName() == 'title1'
            loc.getAddress() == "add1"
            loc.getUserId() == 100L

        cleanup:
            jdbcTemplate.execute("DELETE FROM locations WHERE name = 'title1'")
    }

    def "should delete location"() {

        given:
            jdbcTemplate.execute("INSERT INTO locations(id,name,address,user_id) VALUES(500,'name1','add1',100)")
            jdbcTemplate.execute("INSERT INTO accesses(id,title,location_id,user_id) VALUES(500,'ADMIN',500,200)")

        when:
            locationDao.deleteLocation('name1', 100L).join()

        then:
            def deletedAccess = jdbcTemplate.query("SELECT * FROM accesses WHERE location_id = ?", BeanPropertyRowMapper.newInstance(UserAccess.class), 500L)
            deletedAccess.isEmpty()
            def deletedLocation = jdbcTemplate.query("SELECT * FROM locations WHERE id = ? AND user_id = ?", BeanPropertyRowMapper.newInstance(Location.class), 500L, 100L)
            deletedLocation.isEmpty()
    }

    def "should find locations not shared to user"() {

        when:
            def locToShare = locationDao.findNotSharedToUserLocation(100L, 200L, 200L).join()

        then:
            locToShare.getName() == 'gym'
    }

    def "should find location by id"() {

        when:
            def loc = locationDao.findLocationById(100L).join()

        then:
            loc.getName() == 'home'
    }
}
