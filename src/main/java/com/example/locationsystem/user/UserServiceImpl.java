package com.example.locationsystem.user;


import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User findUserByUsernameAndPassword(String username, String password) {
        return userRepository.findByUsernameAndPassword(username,password);
    }


    @Override
    public void saveUser(User user) {
        user.setPassword(user.getPassword());
        userRepository.save(user);
    }

    @Override
    public User findById(Long id) {
        return userRepository.getById(id);
    }

    @Override
    public List<User> findUsersToShare(Long id) {
        return userRepository.findAllByIdNotLike(id);
    }

    @Override
    public List<User> findAllUsersWithAccessOnLocation(Long locationId, String title, Long id) {
        List<User> users = userRepository.findAllUsersWithAccessOnLocation(locationId, title);
        users.removeIf(user -> user.getId().equals(id));
        return users;
    }

    @Override
    public User findLocationOwner(Long locationId, Long id) {
        User owner = userRepository.findUserByLocationId(locationId);
        if (owner.getId().equals(id)) {
            return null;
        }
        return owner;
    }
}

