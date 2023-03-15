package com.example.locationsystem.user;

public interface UserService {

    User findByUserName(String name);

    void saveUser(User user);

    User findById(Long id);

}
