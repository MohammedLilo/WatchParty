package com.lilo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "default_user_profile_picture")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DefaultUserProfilePicture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private long id;
    @Column(name = "picture_file_name")
    private String pictureFileName;
}
