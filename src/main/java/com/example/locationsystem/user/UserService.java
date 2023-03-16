package com.example.locationsystem.user;

import java.util.List;

public interface UserService {

    User findByUserName(String name);

    void saveUser(User user);

    User findById(Long id);

    List<User> findUsersToShare(Long id);

    List<User> findAllUsersWithAccessOnLocation(Long locationId, String title, Long id);

    User findLocationOwner(Long locationId, Long id);

}
