package com.lilo.repository;

import com.lilo.enums.ERoles;
import com.lilo.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolesRepository extends JpaRepository<Role, Integer> {
Optional<Role> findByName(ERoles roleName);
}
