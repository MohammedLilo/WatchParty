package com.lilo.service;

import com.lilo.model.User;
import com.lilo.operationResult.TableOperationResult;

public interface UserService {
	User findById(long id);

	User findByEmail(String email);

	TableOperationResult save(User user);

	void update(User user);

	void nullifyPartyIdForAllUsers();

	void deleteById(long id);
}
