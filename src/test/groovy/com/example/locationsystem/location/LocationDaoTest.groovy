package com.example.locationsystem.location

import com.example.locationsystem.userAccess.UserAccess
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.Specification

import javax.sql.DataSource
import java.util.concurrent.CompletableFuture

@SpringBootTest
class LocationDaoTest extends Specification {

    LocationDao locationDao
    JdbcTemplate jdbcTemplate

    @Autowired
    DataSource dataSource

    def setup() {

        jdbcTemplate = new JdbcTemplate(dataSource)
        locationDao = new LocationDao(jdbcTemplate)

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
            CompletableFuture<List<Location>> futureResult = locationDao.findAllUserLocations(100L)

        then:
            List<Location> locations = futureResult.get()
            locations.size() == 3
            locations[0].getName() == 'swimming pool'
            locations[1].getName() == 'home'
            locations[2].getName() == 'gym'
    }

    def "should find location in user locations"() {

        when:
            CompletableFuture<Location> futureResult = locationDao.findLocationInUserLocations(100L, 100L)

        then:
            Location location = futureResult.get()
            location.getName() == 'home'
            location.getAddress() == 'test'
    }

    def "should find location by name and userId"() {

        when:
            CompletableFuture<Optional<Location>> futureResult = locationDao.findLocationByNameAndUserId("gym", 100L)

        then:
            futureResult.get().isPresent()
            futureResult.get().get().getAddress() == 'naleczowska'
    }

    def "should save location"() {

        given:
            Location location = new Location("title1", "add1", 100L)

        when:
            CompletableFuture<Location> futureResult = locationDao.saveLocation(location)

        then:
            Location loc = futureResult.get()
            loc != null
            loc.getName() == 'title1'

        cleanup:
            jdbcTemplate.execute("DELETE FROM locations WHERE name = 'title1'")
    }

    def "should delete location"() {

        given:
            jdbcTemplate.execute("INSERT INTO locations(id,name,address,user_id) VALUES(500,'name1','add1',100)")
            jdbcTemplate.execute("INSERT INTO accesses(id,title,location_id,user_id) VALUES(500,'ADMIN',500,200)")

        when:
            locationDao.deleteLocation('name1', 100L)

        then:
            def deletedAccess = jdbcTemplate.query("SELECT * FROM accesses WHERE location_id = ?", BeanPropertyRowMapper.newInstance(UserAccess.class), 500L)
            deletedAccess.isEmpty()
            def deletedLocation = jdbcTemplate.query("SELECT * FROM locations WHERE id = ? AND user_id = ?", BeanPropertyRowMapper.newInstance(Location.class), 500L, 100L)
            deletedLocation.isEmpty()
    }

    def "should find locations not shared to user"() {

        when:
            CompletableFuture<Location> futureResult = locationDao.findNotSharedToUserLocation(100L, 200L, 200L)

        then:
            Location locToShare = futureResult.get()
            locToShare.getName() == 'gym'
    }

    def "should find location by id"() {

        when:
            CompletableFuture<Location> futureResult = locationDao.findLocationById(100L)

        then:
            Location loc = futureResult.get()
            loc.getName() == 'home'
    }
}
