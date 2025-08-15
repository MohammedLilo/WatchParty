package com.lilo.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.lilo.model.User;
import com.lilo.repository.UserRepository;

@Service
public class UserService implements UserDetailsService {
	private final UserRepository repository;

	public UserService(UserRepository repository) {
		this.repository = repository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = repository.findByEmail(username);
		if (user == null)
			throw new UsernameNotFoundException("user details not found for the queried user.");

		String userName = user.getEmail();
		String password = user.getPassword();

		Collection<GrantedAuthority> authorities = Collections
//				.singletonList(new SimpleGrantedAuthority(user.getRole()));
				.singletonList(new SimpleGrantedAuthority("USER"));
		return new org.springframework.security.core.userdetails.User(userName, password, authorities);
	}

}
