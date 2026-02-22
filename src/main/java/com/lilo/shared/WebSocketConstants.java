package com.lilo.shared;

public final class WebSocketConstants {
    public static final String TOPIC_PARTY = "/topic/party";
//    public static final String TOPIC_PARTY_MEMBERS_COUNT = "/topic/watch-party-members-count";

    public static final String TOPIC_PARTY_CHAT = "/topic/chat";
    public static final String QUEUE_ERRORS = "/queue/errors";
    public static final String NOTIFICATIONS_QUEUE = "/queue/notifications";
    public static final String TOPIC_PARTY_MEMBER_EVENTS = "/topic/member-events";
    public static final int PARTY_ID_LENGTH = 36;
    public static final int CHAT_ID_LENGTH = 36;
}
