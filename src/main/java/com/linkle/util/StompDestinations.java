package com.linkle.util;

/** STOMP 구독/발행 목적지 유틸 */
public final class StompDestinations {
    private StompDestinations() {}

    /** 방 구독 프리픽스: /sub/room.{roomId} */
    public static final String SUB_ROOM_PREFIX  = "/sub/room.";

    /** 방 목적지 */
    public static String room(long roomId)  { return SUB_ROOM_PREFIX + roomId; }

    /** 사용자 개인 목적지(필요 시 DM 알림 등에 사용) */
    public static String user(long userId)  { return QUEUE_USER_PREFIX + userId; }

    /** 원시 목적지 직접 사용 */
    public static String of(String raw)     { return raw; }
}
