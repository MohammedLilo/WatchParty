package com.lilo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "watch_parties")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Party {

    @Id
    @Column
    private String id;

    @Column(name = "owner_user_id", nullable = false)
    private long ownerUserId;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "is_private", nullable = false)
    private boolean isPrivate = false;

    @Column(name = "current_video_url")
    private String currentVideoUrl;

    @Column(name = "latest_sync_event_payload", columnDefinition = "TEXT")
    private String latestSyncEventJsonPayload;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "thumbnail_file_name")
    private String thumbnailFileName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id",insertable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_watch_parties_user"))
    private User user;
    public Party(long ownerUserId, String partyName, User ownerUser) {
        this.id = UUID.randomUUID().toString();
        this.ownerUserId = ownerUserId;
        this.name = partyName;
        this.createdAt = Instant.now();
        this.user = ownerUser;
    }
}