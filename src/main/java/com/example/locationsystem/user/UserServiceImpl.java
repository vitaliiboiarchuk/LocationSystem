package com.example.locationsystem.user;


import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User findByUsername(String username) {
        return userDao.findByUsername(username);
    }

    @Override
    public User findUserByUsernameAndPassword(String username, String password) {
        return userDao.findUserByUsernameAndPassword(username,password);
    }


    @Override
    public void saveUser(User user) {
        userDao.saveUser(user);
    }

    @Override
    public User findById(Long id) {
        return userDao.findById(id);
    }

    @Override
    public List<User> findUsersToShare(Long id) {
        return userDao.findUsersToShare(id);
    }

    @Override
    public List<User> findAllUsersWithAccessOnLocation(Long locationId, String title, Long id) {
        return userDao.findAllUsersWithAccessOnLocation(locationId,title,id);
    }

    @Override
    public User findLocationOwner(Long locationId, Long id) {
        return userDao.findLocationOwner(locationId,id);
    }
}

