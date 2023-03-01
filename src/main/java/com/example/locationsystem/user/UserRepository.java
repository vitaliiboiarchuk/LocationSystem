package com.example.locationsystem.user;

import com.example.locationsystem.location.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    @Query("select u from User u where u.id <> ?1")
    List<User> findAllWhereIdNotLike(Long id);

    @Query("select u from User u join u.readOnlyLocations where :location member of u.readOnlyLocations")
    List<User> findAllReadOnlyFriendsOnLocation(Location location);

    @Query("select u from User u join u.adminLocations where :location member of u.adminLocations")
    List<User> findAllAdminFriendsOnLocation(Location location);
}
