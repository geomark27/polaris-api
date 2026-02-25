package com.azenticsys.polaris.config;

import com.azenticsys.polaris.user.entity.User;
import com.azenticsys.polaris.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin.username:admin}")
    private String adminUsername;

    @Value("${app.seed.admin.email:admin@polaris.local}")
    private String adminEmail;

    @Value("${app.seed.admin.password:Admin1234!}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User admin = User.builder()
                    .username(adminUsername)
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .isActive(true)
                    .build();

            userRepository.save(admin);
            log.info("Default admin user created — username: '{}', email: '{}'", adminUsername, adminEmail);
        } else {
            log.debug("DataInitializer: users already exist, skipping seed.");
        }
    }
}
