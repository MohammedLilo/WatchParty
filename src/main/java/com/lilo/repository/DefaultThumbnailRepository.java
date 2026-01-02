package com.lilo.repository;

import com.lilo.model.DefaultThumbnail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DefaultThumbnailRepository extends JpaRepository<DefaultThumbnail, Long> {
    @Query(value = "SELECT * FROM default_thumbnails ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<DefaultThumbnail> findRandomThumbnail();
}
