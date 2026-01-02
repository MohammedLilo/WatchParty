package com.lilo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "default_thumbnails")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DefaultThumbnail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private long id;
    @Column(name = "thumbnail_file_name")
    private String thumbnailFileName;
}
