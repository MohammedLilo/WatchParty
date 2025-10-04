package com.lilo.service;

import com.lilo.model.User;
import com.lilo.model.dto.UserInputDTO;
import com.lilo.operationResult.TableOperationResult;

import java.util.List;
import java.util.Optional;

public interface UserService {
    Optional<User> findById(long id);

    Optional<User> findByEmail(String email);

    boolean ownsParty(long id);

    TableOperationResult save(User user);

    TableOperationResult update(User user, UserInputDTO targetUser);

    TableOperationResult update(User user);

    void nullifyPartyIdForAllUsers();

    void deleteById(long id);

    Optional<User> findFirstByPartyIdOrderByJoinTime(String partyId);
    List<User> findAllByPartyIdOrderByJoinTime(String partyId);
}
