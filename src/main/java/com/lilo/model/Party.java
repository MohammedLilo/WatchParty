package com.lilo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
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
    private String latestSyncEventJson;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public Party(long ownerUserId, String partyName) {
        this.id = UUID.randomUUID().toString();
        this.ownerUserId = ownerUserId;
        this.name = partyName;
        createdAt = Instant.now();
    }
}