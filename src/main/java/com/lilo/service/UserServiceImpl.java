package com.lilo.service;

import com.lilo.model.dto.UserInputDTO;
import com.lilo.operationResult.TableOperationResult;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.lilo.model.User;
import com.lilo.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

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
        if (userRepository.existsByEmailOrPhoneNumber(targetUser.getEmail(), targetUser.getPhoneNumber()))
            return TableOperationResult.fromFailure("this email or phone number is already taken", HttpStatus.CONFLICT.value());

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
