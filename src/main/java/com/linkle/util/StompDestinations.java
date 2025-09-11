package com.linkle.util;

/**
 * STOMP 목적지(토픽/큐) 상수 & 빌더
 * WebSocketConfig:
 *   registry.enableSimpleBroker("/sub", "/queue");
 *   registry.setApplicationDestinationPrefixes("/app");
 */
public final class StompDestinations {
    private StompDestinations() {}

    // ---- 브로커/앱 프리픽스 ----
    public static final String APP_PREFIX   = "/app";
    public static final String SUB_PREFIX   = "/sub";
    public static final String QUEUE_PREFIX = "/queue";

    // ---- 방 브로드캐스트 ----
    // 예) /sub/room.123
    public static final String ROOM_TOPIC_PREFIX = SUB_PREFIX + "/room.";
    public static String room(long roomId) { return ROOM_TOPIC_PREFIX + roomId; }

    // ---- 유저 개인 큐 ----
    // 예) /queue/user.7
    public static final String USER_QUEUE_PREFIX = QUEUE_PREFIX + "/user.";
    public static String user(long userId) { return USER_QUEUE_PREFIX + userId; }

    // ---- 유저 단일 리스트 갱신 토픽 ----
    // 예) /sub/users.7.room-updates
    public static final String USER_ROOM_UPDATES_PREFIX = SUB_PREFIX + "/users.";
    public static String userRoomUpdates(long userId) {
        return USER_ROOM_UPDATES_PREFIX + userId + ".room-updates";
    }

    // ---- 앱 목적지 ----
    public static final String APP_MESSAGE_SEND      = APP_PREFIX + "/message.send";
    public static final String APP_MESSAGE_SEND_PATH = "message.send";

    public static final String APP_READ_SYNC      = APP_PREFIX + "/read.sync";
    public static final String APP_READ_SYNC_PATH = "read.sync";

    public static String of(String raw) { return raw; }
}
