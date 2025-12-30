package com.lilo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "ChatMessage",
        indexes = {
                @Index(name = "idx_ChatMessage_partyId", columnList = "party_id")
        })
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
	private String id;
	private String content;
    @Column(name = "sender_name")
    private String senderName;

//    @Column(name = "sender_id")
//    private long senderId;
    @Column(name = "party_id")
    private String partyId;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "party_id",insertable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_ChatMessage_Party"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Party party;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    @JoinColumn(name = "senderId", nullable = true,foreignKey = @ForeignKey(name = "fk_ChatMessage_User"))
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private User user;

    public ChatMessage(String content, String senderName, String partyId, User user) {
        this.content = content;
        this.senderName = senderName;
        this.partyId = partyId;
        this.user = user;
    }
}
