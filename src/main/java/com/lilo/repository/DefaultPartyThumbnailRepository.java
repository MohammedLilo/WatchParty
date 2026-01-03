package com.lilo.repository;

import com.lilo.model.DefaultPartyThumbnail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DefaultPartyThumbnailRepository extends JpaRepository<DefaultPartyThumbnail, Long> {
    @Query(value = "SELECT * FROM default_party_thumbnails ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<DefaultPartyThumbnail> findRandomly();
}
