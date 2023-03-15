package com.example.locationsystem.role

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import spock.lang.Specification

@DataJpaTest
class RoleRepositoryTest extends Specification {

    @Autowired
    TestEntityManager testEntityManager

    @Autowired
    RoleRepository roleRepository

    def "should find by name"() {
        given:
        def role = new Role("ROLE_ADMIN")
        testEntityManager.persist(role)

        when:
        def result = roleRepository.findByName("ROLE_ADMIN")

        then:
        role.getName() == result.getName()
    }
}
