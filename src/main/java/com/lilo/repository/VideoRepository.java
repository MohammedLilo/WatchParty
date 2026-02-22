package com.lilo.repository;

import java.util.List;

import com.lilo.enums.VideoStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.lilo.model.Video;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface VideoRepository extends JpaRepository<Video, String> {
	List<Video> findByUserId(long userId);

    @EntityGraph(attributePaths = {"user"}) // Eagerly loads the 'user' field in the Video entity
    Page<Video> findAll(Pageable pageable);

//    @Modifying
//    @Transactional
//    @Query(value = """
//                    DELETE FROM Video v
//                    WHERE v.videoFileName = :fileName
//                    AND v.userId = (SELECT u.id FROM User u WHERE u.email = :email)
//                    """)
//    int deleteIfUserIsOwner(@Param("fileName") String fileName, @Param("email") String email);
@Modifying
@Transactional
@Query(value = """
                    DELETE FROM Video v 
                    WHERE v.videoFileName = :fileName 
                    AND v.userId = :userId
                    """)
int deleteIfUserIsOwner(@Param("fileName") String fileName, @Param("userId") long userId);

    @Modifying
    @Transactional
    @Query("UPDATE Video v SET v.status = :status WHERE v.videoFileName = :videoFileName")
    int updateStatus(String videoFileName, VideoStatus status);
    Page<Video> findByStatus(VideoStatus status, Pageable pageable);
}
