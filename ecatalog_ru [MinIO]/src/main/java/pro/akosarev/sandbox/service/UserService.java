package pro.akosarev.sandbox.service;

import org.springframework.stereotype.Service;
import pro.akosarev.sandbox.entity.Product;
import pro.akosarev.sandbox.entity.User;
import pro.akosarev.sandbox.repository.UserRepository;

import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public User findUserById(String userId) {
        return userRepository.findById(userId).orElseGet(() -> {
            User newUser = new User(userId);
            saveUser(newUser);
            return newUser;
        });
    }

    public User saveUser(User user) {
        Optional<User> userFromDB = userRepository.findById(user.getId());

        if (userFromDB.isPresent()) {
            return null;
        }

        userRepository.save(user);
        return user;

    }

}
