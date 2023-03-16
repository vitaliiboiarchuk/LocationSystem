package com.example.locationsystem.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    List<User> findAllByIdNotLike(Long id);

    @Query("select u from User u join UserAccess ua on u.id = ua.user.id where ua.location.id = ?1 and ua.title = ?2")
    List<User> findAllUsersWithAccessOnLocation(Long locationId, String title);

    @Query("select l.user from Location l where l.id = ?1")
    User findUserByLocationId(Long locationId);

}
