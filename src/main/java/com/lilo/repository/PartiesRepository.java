package com.lilo.repository;

import com.lilo.model.Party;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartiesRepository extends JpaRepository<Party, String> {

}
