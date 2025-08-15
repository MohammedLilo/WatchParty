package com.lilo.service;

import com.lilo.model.User;
import com.lilo.operationResult.TableOperationResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Data
public class UserAuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public TableOperationResult save(User user) {
        user.setEmail(user.getEmail().toLowerCase());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userService.save(user);
    }

}
