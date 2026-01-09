package com.lilo.repository;

import com.lilo.model.DefaultUserProfilePicture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DefaultUserProfilePictureRepository extends JpaRepository<DefaultUserProfilePicture, Long> {
    @Query(value = "SELECT * FROM default_user_profile_picture ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<DefaultUserProfilePicture> findRandomly();

    boolean existsByPictureFileName(String pictureFileName);
}
