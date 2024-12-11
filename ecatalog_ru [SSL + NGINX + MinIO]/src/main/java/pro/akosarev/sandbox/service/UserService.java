package pro.akosarev.sandbox.service;

import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import pro.akosarev.sandbox.entity.User;
import pro.akosarev.sandbox.entity.UserAuthority;
import pro.akosarev.sandbox.entity.UserInfo;
import pro.akosarev.sandbox.repository.UserAuthorityRepository;
import pro.akosarev.sandbox.repository.UserInfoRepository;
import pro.akosarev.sandbox.repository.UserRepository;

import java.util.Date;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserAuthorityRepository userAuthorityRepository;

    private final UserInfoRepository userInfoRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       UserAuthorityRepository userAuthorityRepository,
                       UserInfoRepository userInfoRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userAuthorityRepository = userAuthorityRepository;
        this.passwordEncoder = passwordEncoder;
        this.userInfoRepository = userInfoRepository;
    }

    @Transactional
    public void registerUser(User user) {
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);
        user.setHaveProfileImage(false);

        // Сохранение пользователя
        userRepository.save(user);

        // Создаем новую роль
        UserAuthority userAuthority = new UserAuthority();
        userAuthority.setUser(user);
        userAuthority.setAuthority("ROLE_USER");

        // Сохранение роли по умолчанию
        userAuthorityRepository.save(userAuthority);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public void updateUserInfo(User user, String urlImage) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUrlImage(urlImage);
        userInfo.setUrlLastUpdate(new Date());

        // Привязываем запись к пользователю
        userInfo.setUser(user);

        // Сохраняем запись в базе данных
        userInfoRepository.save(userInfo);
    }
}
