package com.lilo.config;

import com.lilo.enums.ERoles;
import com.lilo.model.Role;
import com.lilo.repository.RolesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Slf4j
@Configuration
public class DataInitializerConfig {
    @Bean
    CommandLineRunner initDatabase(RolesRepository roleRepository) {
        return args -> {
            Arrays.stream(ERoles.values()).forEach(role -> {
                if (roleRepository.findByName(role).isEmpty()) {
                    log.info("Creating role {}", role.name());
                    roleRepository.save(new Role(role));
                } else
                    log.info("Role {} is already present", role.name());
            });
        };
    }
}
