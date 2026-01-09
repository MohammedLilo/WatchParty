package com.lilo.service;

import com.lilo.model.dto.UserInputDTO;
import com.lilo.operationResult.TableOperationResult;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.lilo.model.User;
import com.lilo.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    @Override
    public Optional<User> findById(long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findFirstByPartyIdOrderByJoinTime(String partyId) {
        return userRepository.findFirstByPartyIdOrderByPartyJoinTimeAsc(partyId);
    }

    @Override
    public List<User> findAllByPartyIdOrderByJoinTime(String partyId) {
        return userRepository.findAllByPartyIdOrderByPartyJoinTime(partyId);
    }

    @Override
    public void updateProfilePicture(User user, MultipartFile multipartFile) throws IOException {
        String fileName = UUID.randomUUID().toString();
        fileStorageService.save(fileName, multipartFile);
        user.setProfilePicture(fileName);
        userRepository.save(user);
    }

    @Override
    public boolean ownsParty(long id) {
        return userRepository.existsByIdAndPartyIdIsNotNull(id);
    }

    @Override
    public TableOperationResult save(User user) {
        if (userRepository.existsByEmailOrPhoneNumber(user.getEmail(), user.getPhoneNumber()))
            return TableOperationResult.fromFailure("this email or phone number is already taken", HttpStatus.CONFLICT.value());
        userRepository.save(user);
        return TableOperationResult.fromSuccess();
    }

    @Override
    public TableOperationResult update(User user, UserInputDTO targetUser) {
        boolean emailChanging = !Objects.equals(user.getEmail(), targetUser.getEmail());
        boolean phoneChanging = !Objects.equals(user.getPhoneNumber(), targetUser.getPhoneNumber());
        boolean nameChanging = !Objects.equals(user.getName(), targetUser.getFullName());

        if (!emailChanging && !phoneChanging && !nameChanging)
            return TableOperationResult.fromSuccess();

        if (emailChanging && userRepository.existsByEmail(targetUser.getEmail()))
            return TableOperationResult.fromFailure("Email is already taken", HttpStatus.CONFLICT.value());
        
        if (phoneChanging && userRepository.existsByPhoneNumber(targetUser.getPhoneNumber()))
            return TableOperationResult.fromFailure("Phone number is already taken", HttpStatus.CONFLICT.value());

        user.setEmail(targetUser.getEmail());
        user.setPhoneNumber(targetUser.getPhoneNumber());
        user.setName(targetUser.getFullName());

        userRepository.save(user);
        return TableOperationResult.fromSuccess();
    }

    @Override
    public TableOperationResult update(User user) {

        userRepository.save(user);
        return TableOperationResult.fromSuccess();
    }
    @Transactional
    public void nullifyPartyIdForAllUsers() {
        userRepository.nullifyPartyIdForAllUsers();
    }

    @Override
    public void deleteById(long id) {
        userRepository.deleteById(id);
    }


}
